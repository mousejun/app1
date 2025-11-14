package com.community.app_1113

object AppConstants {
    const val PREFS_NAME = "bookmark_prefs"
    const val KEY_IS_FIRST_RUN = "is_first_run"
    const val KEY_SELECTED_STATES = "selected_states"
    const val KEY_ALL_CHECKED = "all_checked"

    // 코치마크 다시 보지 않기 저장을 위한 키
    const val KEY_COACH_MARK_SHOWN = "coach_mark_shown"
    
    // 홈 화면 바로가기 추가 여부를 저장하기 위한 키
    const val KEY_SHORTCUT_ADDED = "shortcut_added"
    
    // 페이지 히스토리 저장을 위한 키
    const val KEY_PAGE_HISTORY = "page_history"

    // 카테고리 상수
    const val CATEGORY_HOT = "🔥 인기/핫 게시판"
    const val CATEGORY_GAME_IT = "🎮 게임/IT 커뮤니티"
    const val CATEGORY_CHAT = "💬 잡담/커뮤니티"
    const val CATEGORY_PHOTO = "📸 사진/특정 주제"
    const val CATEGORY_ENTERTAINMENT = "🎬 연예 / 해외 이슈"
    const val CATEGORY_ETC = "📰 기타"

    val bookmarks = listOf(
        // 🔥 인기/핫 게시판
        BookmarkItem("네이트판", "https://pann.nate.com/", CATEGORY_HOT),
        BookmarkItem("딴지일보", "https://www.ddanzi.com/free", CATEGORY_HOT),
        BookmarkItem("디시인사이드", "https://www.dcinside.com/", CATEGORY_HOT),
        BookmarkItem("보배드림", "https://www.bobaedream.co.kr/list?code=best", CATEGORY_HOT),
        BookmarkItem("뽐뿌", "https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu", CATEGORY_HOT),
        BookmarkItem("아이고수", "https://ygosu.com/", CATEGORY_HOT),
        BookmarkItem("오늘의 유머", "https://m.todayhumor.co.kr/list.php?table=bestofbest", CATEGORY_HOT),
        BookmarkItem("에펨코리아", "https://www.fmkorea.com/", CATEGORY_HOT),
        
        // 🎮 게임/IT 커뮤니티
        BookmarkItem("디미토리", "https://www.dmitory.com/", CATEGORY_GAME_IT),
        BookmarkItem("루리웹", "https://bbs.ruliweb.com/community", CATEGORY_GAME_IT),
        BookmarkItem("인벤", "https://www.inven.co.kr/board/webzine/2097?iskin=webzine", CATEGORY_GAME_IT),
        
        // 💬 잡담/커뮤니티
        BookmarkItem("82쿡", "https://www.82cook.com/entiz/enti.php?bn=15", CATEGORY_CHAT),
        BookmarkItem("가생이닷컴", "https://www.gasengi.com/main/board.php?bo_table=commu08", CATEGORY_CHAT),
        BookmarkItem("다모앙", "https://damoang.net/", CATEGORY_CHAT),
        BookmarkItem("뉴덕", "https://newduck.net/board_CzNT67", CATEGORY_CHAT),
        BookmarkItem("쓰레딕", "https://thredic.com/index.php?mid=all", CATEGORY_CHAT),
        BookmarkItem("이토랜드", "https://www.etoland.co.kr/bbs/board.php?bo_table=freebbs", CATEGORY_CHAT),
        BookmarkItem("인스티즈", "https://www.instiz.net/hot.htm", CATEGORY_CHAT),
        BookmarkItem("클리앙", "https://www.clien.net/service/", CATEGORY_CHAT),
        
        // 📸 사진/특정 주제
        BookmarkItem("SLR클럽", "https://m.slrclub.com/bbs/zboard.php?id=free", CATEGORY_PHOTO),
        
        // 🎬 연예 / 해외 이슈
        BookmarkItem("해연갤", "https://hygall.com/", CATEGORY_ENTERTAINMENT),
        
        // 📰 기타
        BookmarkItem("DVD프라임", "https://dvdprime.com/g2/bbs/all.php", CATEGORY_ETC),
        BookmarkItem("MLB파크", "https://mlbpark.donga.com/mp/best.php?b=mlbtown&m=like", CATEGORY_ETC),
        BookmarkItem("Pgr21", "https://pgr21.com/recommend/0", CATEGORY_ETC),
        BookmarkItem("아카라인", "https://arca.live/", CATEGORY_ETC),
        BookmarkItem("웃긴대학", "https://m.humoruniv.com/main.html", CATEGORY_ETC),
        BookmarkItem("잇싸", "https://itssa.co.kr/hot", CATEGORY_ETC)
    )
}