package tech.myevents.app;

import android.content.SharedPreferences;

import java.util.List;

public class StaticVariables {

    static List listBusinesses, listMessages, listInbox, listEvents, listEventsExclusive, listEventsWaiting, listAds, listAdsWaiting, listPlaying, listAudience,
            listVersionCode,
            listImpressions, listReview, listCommentsInbox, listComments, listTickets, listVerifyContacts, listVerifyBusiness, listFriends, listMoments;

    static List<String> listVerifiedAccounts;

    private static String X = "";
    static String childEvents = "events" + X,
            childApprovalCodes = "approvalCodes" + X,
            childEventsWaiting = "waitingEvents" + X,
            childAds = "ads" + X,
            childAdsWaiting = "waitingAds" + X,
            childImpressions = "impressions" + X,
            childUsers = "users" + X,
            childBusinesses = "businesses" + X,
            childAppVersion = "versionCode" + X,
            childCharges = "broadcastCharges" + X,
            childPromotionCode = "promotionCode" + X,
            childMessages = "messages" + X,
            childWelcomeMessages = "welcomeMessages" + X,
            childVerifiedBusinesses = "verifiedBusinesses" + X,
            childVerifyContacts = "verifyContacts" + X,
            childVerifiedContacts = "verifiedContacts" + X,
            childMoments = "moments" + X,
            childComments = "comments" + X,
            childTickets = "tickets" + X,
            childNotificationsUser = "notificationsUser" + X,
            childNotificationsAdmin = "notificationsAdmin" + X,
            childNotificationTicket = "notificationTicket" + X,
            childPageHits = "pageHits" + X;

    static String receiver = "receiver", sender = "sender", newBroad = "new" + X,
            ticket = "ticket" + X, free = "free" + X, eventWaiting =  "eventWaiting" + X, adWaiting = "adWaiting" + X,
            deleted = "deleted" + X, moment = "moment" + X, event = "event" + X, ad = "ad" + X, business = "business" + X,
            message = "message" + X, friend = "friend" + X, verifyContact = "verifyContact" + X, signUp = "signUp" + X;
    static String playing = "playing" + X, eventExclusive = "eventExclusive" + X, comment = "comment" + X, review = "review" + X;
    static String individual = "individual" + X, commentBroad = "commentBroadcast" + X, commentReview = "commentReview" + X;

    static String adminEmail = "app.myevents@gmail.com";

    static String adminEcocashNumber = "0774876886";

    static AdapterEvent eventsAdapter;

    static AdapterAd adsAdapter;

    static AdapterWaitingAd adsAdapterWaiting;

    static AdapterEventWaiting eventsAdapterWaiting;

    static AdapterInbox inboxAdapter;

    static AdapterMessage messagesAdapter;

    static AdapterBusiness businessesAdapter;

    /*static WaitingAdsAdapter waitingAdsAdapter;

    static PlayingAdapter playingAdapter;

    static FriendsAdapter businessesAdapter;

    static IndividualsAdapter individualsAdapter;

    static ImpressionsAdapter impressionsAdapter;

    static TicketsAdapter ticketAdapter;*/

    static String[] paymentType = {"Ecocash number", "Ecocash cashout", "Ecocash pay merchant"};

    static String[] businessSize = {"1 - 3 Employees", "4 - 10 Employees",
            "11 - 30 Employees", "31 - 50 Employees", "Over 50 Employees"};

    /*static String[] interests = {"Business", "New Music", "Concerts", "Clubbing Scene",
            "Fashion Shows", "Theatre Screening", "Musical Shows", "Tech & Design", "Community"};*/

    static String[] categories = {"Automotive", "Beauty, Spa & Salon", "Clothing & Apparel", "Education",
            "Entertainment", "Event Planning & Service", "Finance & Banking", "Food & Grocery", "Hotel & Lodging",
            "Medical & Health", "Non-profit", "Community", "Technology & Design", "Restaurant", "Shopping & Retail",
            "Travel & Transportation", "Other"};

    static String[] interestCodes = {"1a", "1b", "1c", "1d", "1e", "1f", "1g", "1h", "1i", "1j", "1k", "1l", "1m", "1n", "1o", "1p", "1q"};

    static String[] months = {"", "JAN", "FEB", "MAR", "Apr", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};

    static String[] monthsSmall = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    static String[] locations = {"Beitbridge", "Bindura", "Bulawayo", "Chegutu", "Chinhoyi",
            "Chipinge", "Chiredzi", "Chitungwiza", "Gwanda", "Gweru", "Harare", "Kadoma",
            "Kariba", "Karoi", "Kwekwe", "Marondera", "Masvingo", "Mutare", "Nyanga", "Plumtree",
            "Rusape", "Shurugwi", "Victoria Falls", "Zvishavane", "Hwange", "Norton", "Triangle",
            "Birchenough Bridge", "Buhera", "Cashel", "Tizvione", "Hauna", "Nyazura",
            "Rusape", "Glendale", "Guruve", "Mount Darwin", "Mvurwi", "Shamva", "Matepatepa", "Mazowe",
            "Acturus", "Beatrice", "Bromley", "Ruwa", "Kotwa", "Nharira", "Goromozi", "Macheke",
            "Mahusekwa", "Suswe",
            "Wedza",
            "Mutoko",
            "Murewa",
            "Epworth",
            "Juru",
            "Sadza",
            "Makosa",
            "Makaha",
            "Bondamakara",
            "Headlands",
            "Nyamapanda",
            "Alaska",
            "Banket",
            "Battlefields",
            "Bumi Hills",
            "Cape Haig",
            "Chakari",
            "Charara",
            "Chirundu",
            "Darwendale",
            "Doma",
            "Eiffel Flats",
            "Eldorado",
            "Feock",
            "Gadzema",
            "Golden Valley",
            "Kildonan",
            "Lion's Den",
            "Madadzi",
            "Magunje",
            "Makuti",
            "Makwiro",
            "Mhangura",
            "Mubayira",
            "Munyati",
            "Muriel",
            "Murombedzi",
            "Mutorashanga",
            "Mwami",
            "Orlando Heights",
            "Raffingora",
            "Sanyati",
            "Selous",
            "Shackleton",
            "Tashinga",
            "Tengwe",
            "Trelawney",
            "Umsweswe",
            "Unsworth",
            "Vanad",
            "Venice",
            "Vuti",
            "Zave",
            "Bikita",
            "Bubye River",
            "Buffalo Range",
            "Chatsworth",
            "Chivi",
            "Felixburg",
            "Gaths Mine",
            "Glenclova",
            "Glenlivet",
            "Gutu",
            "Basera",
            "Gurajena",
            "Gwengwerere GP",
            "Hippo Valley",
            "Mabalauta",
            "Maranda",
            "Mashava",
            "Mbizi",
            "Mupandawana",
            "Mwenezi",
            "Ndanga",
            "Nemanwa",
            "Ngomahuru",
            "Ngundu",
            "Renco",
            "Zimuto Siding",
            "Rutenga",
            "Sango",
            "Soti-Source",
            "Tswiza",
            "Zaka",
            "Musekiwa",
            "Bembezi",
            "Binga",
            "Dagamela",
            "Deka Drum",
            "Dete",
            "Eastnor",
            "Inyati",
            "Kamativi",
            "Kariyangwe",
            "Kazungula",
            "Kenmaur",
            "Lonely Mine",
            "Lupane",
            "Lusulu",
            "Matesti",
            "Mlibizi",
            "Msuna",
            "Nkayi",
            "Ntabazinduna",
            "Nyamandlovu",
            "Pandamatenga",
            "Queen's Mine",
            "Shangani",
            "Siabuwa",
            "Tsholotsho",
            "Turk Mine",

            "Antelope Mine",
            "Blanket",
            "Colleen Bawn",
            "Esimbomvu",
            "Esigodini",
            "Figtree",
            "Filabusi",
            "Fort Rixon",
            "Fort Usher",
            "Kame",
            "Kezi",
            "Mangwe",
            "Maphisa",
            "Marula",
            "Matopo",
            "Mazunga",
            "Mbalabala",
            "Mphoengs",
            "Ngwesi",
            "Nsiza",
            "Towla",
            "Tuli",
            "Vubachikwe",
            "West Nicholson",

            "Bannockburn",
            "Buchwa",
            "Chirumanzu",
            "Chivhu",
            "Copper Queen",
            "Empress Mine",
            "Featherstone",
            "Gokwe",
            "Guinea Fowl",
            "Hunters Road",
            "Ingezi",
            "Insukamini",
            "Lalapanzi",
            "Lower Gweru",
            "Mberengwa",
            "Mvuma",
            "New Featherstone",
            "Njelele",
            "Redcliff",
            "Sherwood",
            "Silobela",
            "Somabhula",
            "Zhombe"};


}