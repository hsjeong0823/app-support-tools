package com.hsjeong.supporttools.utils

/**
 * 딥링크 정보 데이터 클래스
 * @param title 리스트에 표시될 이름 (예: "상품 상세 페이지")
 * @param uri 실제 실행될 딥링크 URI (예: "myapp://product/123")
 */
data class DeepLinkData(
    val title: String,
    val uri: String
)

object DeepLinkManager {
    private val deepLinkList = mutableListOf<DeepLinkData>()

    /**
     * 외부(App 모듈)에서 테스트할 딥링크 리스트를 설정합니다.
     */
    fun setDeepLinkList(list: List<DeepLinkData>) {
        deepLinkList.clear()
        // 고정 테스트 페이지 추가
        deepLinkList.add(DeepLinkData("테스트 페이지", "https://siksik.netlify.app/starbuckstest"))
        deepLinkList.addAll(list)
    }

    fun getDeepLinkList(): List<DeepLinkData> = deepLinkList
}