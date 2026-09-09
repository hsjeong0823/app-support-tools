package com.hsjeong.supporttools.config

class AppSupportConfig private constructor(
    val enableScreenNameOverLay: Boolean,
    val enableLogViewer: Boolean,
    val enableNetworkLog: Boolean,
    val enableUrlSwitching: Boolean,
) {
    class Builder {
        private var enableScreenNameOverLay: Boolean = true
        private var enableLogViewer: Boolean = true
        private var enableNetworkLog: Boolean = true
        private var enableUrlSwitching: Boolean = true

        fun enableScreenNameOverLay(enable: Boolean) = apply {
            this.enableScreenNameOverLay = enable
        }

        fun enableLogViewer(enable: Boolean) = apply {
            this.enableLogViewer = enable
        }

        fun enableNetworkLog(enable: Boolean) = apply {
            this.enableNetworkLog = enable
        }

        fun enableUrlSwitching(enable: Boolean) = apply {
            this.enableUrlSwitching = enable
        }


        fun build(): AppSupportConfig {
            return AppSupportConfig(
                enableScreenNameOverLay,
                enableLogViewer,
                enableNetworkLog,
                enableUrlSwitching,
            )
        }
    }
}
