package com.teknokote.ess.controller;

public final class EndPoints {
    private EndPoints() {
    }
    public static final String ID = "/{id}";
    public static final String CUSTOMER_ACCOUNT_ID = "/{customerAccountId}";
    /**
     * Les EndPoints pour les http uploads à partir du contrôleur
     */
    public static final String UPLOAD = "/upload";
    public static final String UPLOAD_PUMP_TRANSACTION = "/pumpTransaction";
    public static final String UPLOAD_TANK_MEASUREMENT = "/tankMeasurement";
    public static final String UPLOAD_CONFIG = "/config";
    public static final String UPLOAD_IN_TANK_DELIVERY = "/inTankDelivery";
    public static final String UPLOAD_STATUS = "/status";
    public static final String UPLOAD_ALERT_RECORD = "/alertRecord";
    /**
     * Les EndPoints pour les stations
     */
    public static final String ADD = "/add";
    public static final String DEACTIVATE = "/deactivate/{id}";
    public static final String ACTIVATE = "/activate/{id}";// id de l'entité cible de l'activation
    public static final String UPDATE = "/update"; // id de l'entité cible est intégré dans le dto
    public static final String GENERATE = "/generate";
    public static final String RESET = "/reset";
    public static final String START = "/start";
    public static final String STOP = "/stop";
    public static final String LIST_BY_ACTIF = "/{actif}";
    public static final String LIST_BY_FILTER = "/filter";
    public static final String SEARCH = "/search";
    public static final String SEARCH_MASTER_USER = "/admin/search";

    public static final String LIST_CREATOR = "/creator";
    public static final String INFO_OLD = "/info";
    public static final String INFO = ID;

    /**
     * Les EndPoints pour les rotations
     */
    public static final String VALID = "/valid";

    /**
     * Les root Endpoints
     */
    public static final String CUSTOMER_ACCOUNT_ROOT = "/customerAccount";
    public static final String CUSTOMER_ACCOUNT_LIST = "/{customerAccountId}/list";
    public static final String USERS_LOG ="/{customerAccountId}/user/log/{userId}";
    public static final String STATION_ROOT = "/station";
    public static final String SHIFT_PLANNING_ROOT = STATION_ROOT + "/{stationId}/shiftPlanning";
    public static final String PUMP_ATTENDANT_ROOT = STATION_ROOT + "/{stationId}/pumpAttendant";
    public static final String STATION_INFO = ID + INFO_OLD;
    public static final String PUMP_ATTENDANT_INFO = ID + INFO_OLD;
    public static final String USER_ROOT = "/user";
    public static final String USER_LIST = "/{customerAccountId}/list";
    public static final String LIST_OF_TEAM = "/{shiftRotationId}/teams";
    public static final String LIST_OF_TEAM_BY_ROTATION = "/shiftRotation/{shiftRotationId}";
    public static final String REFERENCE_DATA_ROOT = "/referenceData";
    public static final String LIST_OF_STATION = "/list";
    public static final String CUSTOMER_ACCOUNT_STATION_ROOT = CUSTOMER_ACCOUNT_ROOT + "/{customerAccountId}/station";
    public static final String AFFECTED_PUMP_ATTENDANT_ROOT = "/affectedPumpAttendant";
    public static final String PAYMENT_METHOD_ROOT = CUSTOMER_ACCOUNT_ROOT + "/{customerAccountId}/paymentMethod";
    public static final String PUMP_ATTENDANT_COLLECTION_SHEET_ROOT = "/pumpAttendantCollectionSheet";
    public static final String PUMP_ATTENDANT_TEAM_ROOT = STATION_ROOT + "/{stationId}/pumpAttendantTeam";
    public static final String SHIFT_PLANNING_EXECUTION_ROOT = STATION_ROOT + "/{stationId}/shiftPlanningExecution";
    public static final String SHIFT_PLANNING_EXECUTION_DETAIL_ROOT = STATION_ROOT + "/{stationId}/shiftPlanningExecutionDetail";
    public static final String SHIFT_ROTATION_ROOT = STATION_ROOT + "/{stationId}/shiftRotation";
    public static final String PUMP_ATTENDANT_SALES = "{id}/sales";
    public static final String PERIOD_BY_ROTATION = "/periods";
    public static final String FUEL_GRADE_PRICE = "/{stationId}/fuelGradePrice";
    public static final String GET_DATE_TIME = "/{stationId}/date";
    public static final String GET_UPLOADED_INFORMATION = "/{ptsId}";
    public static final String PLAN_FUEL_GRADE_PRICE_CHANGE = "/{stationId}/planFuelGradePriceChange";
    public static final String FUEL_GRADE_LIST = "/{stationId}/fuelGrade";
    public static final String FUEL_GRADE_INFO = "/{stationId}/fuelGrade/{id}";
    /**
     * Les EndPoints pour les CustomerAccount
     */

    //Gestion des comptes clients
    public static final String USER_INFO = ID + INFO_OLD;
    public static final String USER_FUNCTION = "/{id}/functions";
    public static final String FORGET_PASSWORD = "/forgot-password";
    public static final String RESET_PASSWORD = "/reset-password";

    public static final String CUSTOMER_ACCOUNT_DEACTIVATE = DEACTIVATE;
    public static final String CUSTOMER_ACCOUNT_ACTIVATE = ACTIVATE;

    //Gestion des utilisateurs attachés a un compte client
    public static final String CUSTOMER_ACCOUNT_CREATOR_LIST = CUSTOMER_ACCOUNT_ID + LIST_CREATOR;
    public static final String CUSTOMER_ACCOUNT_STATIONS_LIST = STATION_ROOT;
    public static final String CUSTOMER_ACCOUNT_USER_ROOT = CUSTOMER_ACCOUNT_ID + USER_ROOT;
    public static final String CUSTOMER_ACCOUNT_INFO = CUSTOMER_ACCOUNT_ID + INFO_OLD;
    public static final String CUSTOMER_ACCOUNT_USER_ADD = CUSTOMER_ACCOUNT_USER_ROOT + ADD;
    public static final String CUSTOMER_ACCOUNT_USER_DEACTIVATE = CUSTOMER_ACCOUNT_USER_ROOT + DEACTIVATE;
    public static final String CUSTOMER_ACCOUNT_USER_ACTIVATE = CUSTOMER_ACCOUNT_USER_ROOT + ACTIVATE;
    public static final String CUSTOMER_ACCOUNT_USER_LIST = CUSTOMER_ACCOUNT_USER_ROOT;
    public static final String CUSTOMER_ACCOUNT_USER_LIST_BY_ACTIF = CUSTOMER_ACCOUNT_USER_ROOT + LIST_BY_ACTIF;
    public static final String CUSTOMER_ACCOUNT_USER_UPDATE = CUSTOMER_ACCOUNT_USER_ROOT + UPDATE;

    /**
     * Les EndPoints de configuration
     */
    public static final String CONFIGURATION_ROOT = "/configuration";
    public static final String CONFIGURATION_PUMPS = "/pump/{idCtr}";
    public static final String CONFIGURATION_NOZZLES = "/nozzle/{idCtr}";
    public static final String CONFIGURATION_TANKS = "/tank/{idCtr}";
    public static final String CONFIGURATION_FUEL_GRADES = "/fuelGrade/{idCtr}";
    public static final String CONFIGURATION_READERS = "/reader/{idCtr}";
    public static final String CONFIGURATION_PROBES = "/probe/{idCtr}";
    public static final String LIST_OF_NOZZLES_BY_PUMP = "/nozzleByPump/{idCtr}/{idPump}";
    /**
     * Les EndPoints de Transactions
     */
    public static final String LIST_OF_TRANSACTIONS = "/transaction/{idCtr}";
    public static final String LIST_OF_TRANSACTIONS_EXCEL = "/transaction/{idCtr}/excel";
    public static final String LIST_OF_TRANSACTIONS_PDF ="/transaction/{idCtr}/pdf";
    public static final String LIST_OF_DELIVERY = "/delivery/{idCtr}";
    public static final String LIST_OF_DELIVERY_EXCEL = "/delivery/{idCtr}/excel";
    public static final String LIST_OF_DELIVERY_PDF = "/delivery/{idCtr}/pdf";

    public static final String LIST_OF_MEASUREMENT = "/measurement/{idCtr}";
    public static final String LIST_OF_MEASUREMENT_EXCEL = "/measurement/{idCtr}/excel";
    public static final String LIST_OF_MEASUREMENT_PDF = "/measurement/{idCtr}/pdf";
    /**
     * Les EndPoints de version
     */
    public static final String VERSION = "/version";
    /**
     * Les EndPoints de chart
     */
    public static final String CHART_ROOT = "/data";
    public static final String CHART_FUEL_GRADE_BY_PUMP_AND_PERIOD = "/sales/{idCtr}";
    public static final String CHART_TANK_LEVEL_BY_PERIOD = "/tankLevelByPeriod/{idCtr}";
    public static final String CHART_TANK_MEASUREMENT_BY_PERIOD = "/tankMeasurementByPeriod/{idCtr}";
    public static final String CHART_TANK_MEASUREMENT_AND_LEVEL_BY_PERIOD = "/tankMeasurementAndLevelByPeriod/{idCtr}";
    public static final String LIST_OF_TANK_BY_ID_CONF = "/allTankByIdC/{idCtr}";
    /**
     * Les EndPoints pour users
     */
    public static final String LOGIN = "/login";
    public static final String IMPERSONATE = "/impersonate/{targetUserId}";
    public static final String EXIT_IMPERSONATION = "/impersonate/{targetUserId}/exit";
    public static final String GET_BY_USERNAME = "/username/{username}";
    public static final String GET_BY_EMAIL = "/email/{email}";
    public static final String UPDATE_CONTACT = "/updateContact";
    public static final String PROFILE = "/profile";

    /**
     * Les EndPoints de stat of measurement du tank
     */
    public static final String STAT_ROOT = "/stat";
    public static final String LAST_DELIVERY = "/lastDelivery/{idCtr}/{tank}";
    public static final String STAT_TANK_MEASUREMENT = "/tank/{ptsId}";
    public static final String GET_ALL_SALES_BY_CONTROLLER = "/sales/{idCtr}";
    public static final String GET_ALL_SALES_BY_PUMP = "/salesByGrades/{idCtr}/{pumpId}";
    public static final String GET_ALL_SALES_BY_GRADES = "/sales/fuelName/{idCtr}";

    /**
     * Les EndPoints pour les PumpAttendant
     */
    public static final String GET_BY_TAG = "/tag/{tag}";
    public static final String GET_BY_MATRICULE = "/matricule/{matricule}";
    public static final String UPDATE_SHEET = "/sheet/update";
    public static final String LOCK = "/lock";
    public static final String UNLOCK = "/unlock";
    public static final String UPDATE_LIST = "/list/update";
    public static final String UPDATE_DETAILS = "/updateDetails";
    public static final String PLANNED_FUEL_GRADE_PRICE_CHANGE_LIST = "/{stationId}/plannedFuelGrade";
    public static final String EXECUTED_FUEL_GRADE_PRICE_CHANGE_LIST = "/{stationId}/executedFuelGrade";
    public static final String CANCEL_PLANNED_FUEL_GRADE_PRICE_CHANGE = "/{stationId}/plannedFuelGrade/{id}/cancel";
    public static final String UPDATE_PLANNED_FUEL_GRADE_PRICE_CHANGE = "/{stationId}/plannedFuelGrade/update";
    public static final String FUEL_GRADE_PRICE_CHANGE_REQUEST_INFO = "/{stationId}/fuelGradePriceRequest/{id}";
    public static final String IMAGES = "/images/{imageName}";
    public static final String WORK_DAY_SHIFT_PLANNING_ROOT = "/workDayShiftPlanning";
    public static final String WORK_DAY_SHIFT_PLANNING_EXECUTION_ROOT = "/workDayShiftPlanningExecution";
    public static final String COUNTRIES = "/countries";
    public static final String DELETE_ROTATION = "{shiftRotationId}/delete";
    public static final String EXPORT = "/export";
    public static final String FAILED_TRANSACTIONS = "/failedTransactions";
    public static final String PROCESS = "/process";
}
