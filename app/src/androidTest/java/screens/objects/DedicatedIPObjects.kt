package screens.objects

import com.kape.sidemenu.ui.screens.mobile.SideMenuContent
import screens.helpers.UiAutomatorObjectFinder
import screens.helpers.UiAutomatorStepsHelper

object DedicatedIPObjects {

    val dedicatedIPField = UiAutomatorObjectFinder.findByResourceId(":DedicatedIPScreen:dip_text_field")
    val activateButton = UiAutomatorObjectFinder.findByResourceId(":DedicatedIPScreen:activate_button")
    val dedicatedIPFlag = UiAutomatorObjectFinder.findByResourceId(":DedicatedIPScreen:dip_flag")
    val dedicatedIPServerName = UiAutomatorObjectFinder.findByResourceId(":DedicatedIPScreen:dip_server_name")

    fun navigateToDedicatedIPPage(){
        MainScreenObjects.sideMenu.click()
        SideMenuObjects.dedicatedIP.click()
    }

    fun activateDedicatedIPToken(dipToken: String) {
        UiAutomatorStepsHelper.inputTextInField(dedicatedIPField, dipToken)
        activateButton.clickAndWaitForNewWindow()
    }
}
