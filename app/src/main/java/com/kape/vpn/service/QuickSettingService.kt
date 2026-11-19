package com.kape.vpn.service

import com.kape.vpn.system.VpnTileService

// Quick Settings remembers added tiles by component name, so this keeps mobile-android's name;
// renaming it would remove the tile from users' panels when they update.
class QuickSettingService : VpnTileService()