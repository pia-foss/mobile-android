import re
import sys
import shutil
import urllib.request
import xml.etree.ElementTree as ET
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime

TOML_PATH = "gradle/libs.versions.toml"

# Libraries not on Maven Central - skip these
SKIP_MODULES = {
    "com.kape.android",         # private repo
    "com.amazon.device",        # Amazon SDK
    "com.madgag.spongycastle",  # check manually
}

UNSTABLE_KEYWORDS = ["alpha", "beta", "rc", "cr", "preview", "dev", "eap", "snapshot"]

def is_stable(version: str) -> bool:
    return not any(kw in version.lower() for kw in UNSTABLE_KEYWORDS)

def should_skip(group: str) -> bool:
    return any(group.startswith(s) for s in SKIP_MODULES)

def get_latest_stable_version(group, artifact):
    urls = [
        f"https://repo1.maven.org/maven2/{group.replace('.', '/')}/{artifact}/maven-metadata.xml",
        f"https://dl.google.com/dl/android/maven2/{group.replace('.', '/')}/{artifact}/maven-metadata.xml",
    ]
    for url in urls:
        try:
            with urllib.request.urlopen(url, timeout=8) as r:
                tree = ET.parse(r)
                all_versions = [v.text for v in tree.findall(".//version") if v.text]
                stable = [v for v in all_versions if is_stable(v)]
                if stable:
                    return stable[-1]
        except:
            continue
    return None

def parse_toml(path):
    """
    Returns:
      versions_map:  { ref_key: current_version }
      lib_to_ref:    { "group:artifact": ref_key }        (version.ref libs)
      lib_direct:    { "group:artifact": current_version } (inline-versioned libs)
    """
    with open(path) as f:
        content = f.read()

    versions_map = {}
    versions_block = re.search(r'\[versions\](.*?)(?=\n\[|\Z)', content, re.DOTALL)
    if versions_block:
        for line in versions_block.group(1).splitlines():
            m = re.match(r'^\s*([\w-]+)\s*=\s*"([^"]+)"', line)
            if m:
                versions_map[m.group(1)] = m.group(2)

    lib_to_ref = {}
    lib_direct = {}
    libraries_block = re.search(r'\[libraries\](.*?)(?=\n\[|\Z)', content, re.DOTALL)
    if libraries_block:
        for line in libraries_block.group(1).splitlines():
            mod_match   = re.search(r'module\s*=\s*"([^"]+)"', line)
            group_match = re.search(r'group\s*=\s*"([^"]+)"', line)
            name_match  = re.search(r'name\s*=\s*"([^"]+)"', line)
            ref_match   = re.search(r'version\.ref\s*=\s*"([^"]+)"', line)
            ver_match   = re.search(r'\bversion\s*=\s*"([^"]+)"', line)

            if mod_match:
                module = mod_match.group(1)
            elif group_match and name_match:
                module = f"{group_match.group(1)}:{name_match.group(1)}"
            else:
                continue

            if ref_match:
                lib_to_ref[module] = ref_match.group(1)
            elif ver_match:
                lib_direct[module] = ver_match.group(1)

    return versions_map, lib_to_ref, lib_direct

def check(module, current):
    parts = module.split(":")
    if len(parts) != 2:
        return module, current, None, "Bad format"
    group, artifact = parts
    if should_skip(group):
        return module, current, None, "Skipped"
    latest = get_latest_stable_version(group, artifact)
    if not latest:
        return module, current, None, "Not found"
    if not is_stable(current):
        return module, current, latest, "Non-stable"
    if latest == current:
        return module, current, latest, "Up to date"
    return module, current, latest, "OUTDATED"

def apply_updates(path, version_updates, direct_updates):
    """Write updated versions back into the TOML file."""
    backup = f"{path}.bak.{datetime.now().strftime('%Y%m%d_%H%M%S')}"
    shutil.copy2(path, backup)
    print(f"\n💾 Backup saved to: {backup}")

    with open(path) as f:
        content = f.read()

    updated = 0

    # Update entries in [versions] block
    for ref_key, new_version in version_updates.items():
        pattern = rf'^(\s*{re.escape(ref_key)}\s*=\s*)"[^"]+"'
        new_content, count = re.subn(pattern, rf'\g<1>"{new_version}"', content, flags=re.MULTILINE)
        if count:
            content = new_content
            updated += count

    # Update inline version = "..." in [libraries] block
    for module, new_version in direct_updates.items():
        pattern = rf'(module\s*=\s*"{re.escape(module)}"[^\n]*\bversion\s*=\s*)"[^"]+"'
        new_content, count = re.subn(pattern, rf'\g<1>"{new_version}"', content)
        if count:
            content = new_content
            updated += count

    with open(path, "w") as f:
        f.write(content)

    print(f"✅ Updated {updated} version(s) in {path}\n")

def status_icon(s):
    return {"OUTDATED": "⬆️ ", "Non-stable": "⚠️ ", "Up to date": "✅", "Skipped": "⏭️ ", "Not found": "❓"}.get(s, "⚠️ ")

def main():
    update_mode = "--update" in sys.argv or "-u" in sys.argv

    print(f"\n📦 Checking dependencies in {TOML_PATH}...")
    if update_mode:
        print("🔄 Update mode — will rewrite libs.versions.toml with latest stable versions\n")
    else:
        print("👀 Dry run — pass --update to apply changes\n")

    versions_map, lib_to_ref, lib_direct = parse_toml(TOML_PATH)

    # all_modules: module -> (current_version, ref_key_or_None)
    all_modules = {}
    for module, ref in lib_to_ref.items():
        all_modules[module] = (versions_map.get(ref, "?"), ref)
    for module, ver in lib_direct.items():
        all_modules[module] = (ver, None)

    # results: (module, current, latest, status, ref_key)
    results = []
    with ThreadPoolExecutor(max_workers=10) as executor:
        futures = {executor.submit(check, mod, info[0]): (mod, info[1]) for mod, info in all_modules.items()}
        for future in as_completed(futures):
            mod, ref_key = futures[future]
            module, current, latest, status = future.result()
            results.append((module, current, latest, status, ref_key))

    order = {"OUTDATED": 0, "Non-stable": 1, "Up to date": 2, "Not found": 3, "Skipped": 4, "Bad format": 5}
    results.sort(key=lambda x: (order.get(x[3], 9), x[0]))

    outdated  = [r for r in results if r[3] == "OUTDATED"]
    nonstable = [r for r in results if r[3] == "Non-stable"]
    uptodate  = [r for r in results if r[3] == "Up to date"]
    skipped   = [r for r in results if r[3] in ("Skipped", "Not found")]

    col1, col2, col3, col4 = 50, 22, 22, 28
    print(f"{'Library':<{col1}} {'Current':<{col2}} {'Latest Stable':<{col3}} {'Version Key':<{col4}} Status")
    print("-" * (col1 + col2 + col3 + col4 + 10))
    for module, current, latest, status, ref_key in results:
        icon = status_icon(status)
        key_label = ref_key if ref_key else "(inline)"
        print(f"{module:<{col1}} {current:<{col2}} {str(latest or ''):<{col3}} {key_label:<{col4}} {icon} {status}")

    print("\n" + "=" * (col1 + col2 + col3 + 10))
    print(f"  ⬆️  Outdated:         {len(outdated)}")
    print(f"  ⚠️  Using non-stable:  {len(nonstable)}")
    print(f"  ✅ Up to date:        {len(uptodate)}")
    print(f"  ⏭️  Skipped/not found: {len(skipped)}")

    if not update_mode:
        print(f"\n💡 Run with --update to apply all {len(outdated)} stable update(s)\n")
        return

    if not outdated:
        print("\n✅ Nothing to update.\n")
        return

    # Build update maps — prefer updating the [versions] ref entry so all
    # libraries sharing the same ref get updated in one line change
    version_updates = {}  # ref_key -> new_version
    direct_updates  = {}  # module  -> new_version

    for module, current, latest, status, ref_key in outdated:
        if module in lib_to_ref:
            ref = lib_to_ref[module]
            # If multiple libs share a ref, use the highest latest version seen
            if ref not in version_updates or latest > version_updates[ref]:
                version_updates[ref] = latest
        elif module in lib_direct:
            direct_updates[module] = latest

    print(f"\n⬆️  Applying updates:")
    for ref, ver in sorted(version_updates.items()):
        old = versions_map.get(ref, "?")
        print(f"   [versions] {ref}: {old} → {ver}")
    for mod, ver in sorted(direct_updates.items()):
        print(f"   [libraries] {mod}: {lib_direct[mod]} → {ver}")

    apply_updates(TOML_PATH, version_updates, direct_updates)

if __name__ == "__main__":
    main()
