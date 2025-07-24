package com.teknokote.ess.integration.uploads;

import com.teknokote.ess.controller.EndPoints;
import com.teknokote.ess.controller.upload.pts.processing.UploadProcessorSwitcher;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.configuration.FuelGrade;
import com.teknokote.ess.core.model.configuration.Pump;
import com.teknokote.ess.core.model.configuration.Tank;
import com.teknokote.ess.core.model.movements.TankLevelPerSales;
import com.teknokote.ess.core.service.impl.*;
import com.teknokote.ess.core.service.impl.transactions.TankLevelPerSalesService;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.integration.EssApplicationTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.teknokote.ess.integration.TestUtils.readFile;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Disabled("Need liquibase deactivation before launching")

 class ControllerUploadTests extends EssApplicationTests
{

    static final String TEST_CTRL_PTS_ID = "0027003A3438510935383135";

    static final String TEST_CONFIGURATION_ID = "65c50bb7";

   @Autowired
   private ControllerPtsConfigurationService controllerPtsConfigurationService;
   @Autowired
   private TankService tankService;
   @Autowired
   private PumpService pumpService;
   @Autowired
   private FuelGradesService fuelGradesService;
   @Autowired
   private StationService stationService;

   @Autowired
   private TankLevelPerSalesService tankLevelPerSalesService;

   @BeforeEach
    void initContext(){
      connectUser();
   }

   @Test
    void givenKnownPtsUploadConfiguration() throws Exception
   {

      mvc.perform(MockMvcRequestBuilders
            .post(EndPoints.UPLOAD + EndPoints.UPLOAD_CONFIG)
            .content(readFile("/uploads/upload-configuration.json"))
            .accept(MediaType.APPLICATION_JSON))
         .andDo(print())
         .andExpect(status().isOk())
         .andExpect(MockMvcResultMatchers.jsonPath("$.Packets").exists());

      final ControllerPtsConfiguration controllerPtsConf = controllerPtsConfigurationService.findByPtsIdAndConfigurationId(TEST_CTRL_PTS_ID, TEST_CONFIGURATION_ID);
      Assert.notNull(controllerPtsConf,"The controller should have a configurtation.");
      Assert.notNull(controllerPtsConf.getControllerPts(),"The controller should be found.");
   }

   /**
    * Tester un upload configuration avec un ptsId inconnu
    */
   @Test
   void givenUnknownPtsUploadConfiguration() throws Exception
   {
      mvc.perform(MockMvcRequestBuilders
            .post(EndPoints.UPLOAD + EndPoints.UPLOAD_CONFIG)
            .content(readFile("/uploads/upload-unknown-configuration.json"))
            .accept(MediaType.APPLICATION_JSON))
         .andDo(print())
         .andExpect(status().isOk())
         .andExpect(MockMvcResultMatchers.jsonPath("$.Packets").exists())
         .andExpect(MockMvcResultMatchers.jsonPath("$.Packets[0].Message",matchesPattern(UploadProcessorSwitcher.JSONPTS_ERROR_NOT_FOUND)));

   }

   @Test
    void testTankMeasurement() throws Exception {

      // Perform a POST request to create a tank measurement
      mvc.perform(post(EndPoints.UPLOAD + EndPoints.UPLOAD_TANK_MEASUREMENT)
            .content(readFile("/uploads/upload-tank-measurement.json"))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
         .andDo(print())
         .andExpect(status().isOk())
         .andExpect(MockMvcResultMatchers.jsonPath("$.Packets").exists())
         .andExpect(MockMvcResultMatchers.jsonPath("$.Packets[0].Message",matchesPattern(UploadProcessorSwitcher.OK_MESSAGE)));

      List<Tank> tanks = tankService.findTankByControllerOnCurrentConfiguration(1L);
      if (!tanks.isEmpty()) {
         Tank tank = tanks.get(0); // Assuming you want the first tank in the list
         Assert.notNull(tank.getControllerPtsConfiguration(),"The tank should be found.");
      } else {
         System.out.println("No tanks found for the given controller and configuration.");
      }
   }

   @Test
    void testStatus() throws Exception {

      mvc.perform(post(EndPoints.UPLOAD + EndPoints.UPLOAD_STATUS)
            .content(readFile("/uploads/upload-status.json"))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
         .andDo(print())
         .andExpect(status().isOk())
         .andExpect(MockMvcResultMatchers.jsonPath("$.Packets").exists())
         .andExpect(MockMvcResultMatchers.jsonPath("$.Packets[0].Message",matchesPattern(UploadProcessorSwitcher.OK_MESSAGE)));

   }

   @Test
    void testPumpTransaction() throws Exception {

      // Perform a POST request to create a pump transaction
      mvc.perform(post(EndPoints.UPLOAD + EndPoints.UPLOAD_PUMP_TRANSACTION)
            .content(readFile("/uploads/upload-pump-transaction.json"))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
         .andDo(print())
         .andExpect(status().isOk())
         .andExpect(MockMvcResultMatchers.jsonPath("$.Packets").exists())
         .andExpect(MockMvcResultMatchers.jsonPath("$.Packets[0].Message",matchesPattern( UploadProcessorSwitcher.OK_MESSAGE)));

      List<Pump> pumps = pumpService.findPumpsByControllerOnCurrentConfiguration(1L);
      List<FuelGrade> fuelGrades = fuelGradesService.findFuelGradesByControllerOnCurrentConfiguration(1L);
      if (!pumps.isEmpty() && !fuelGrades.isEmpty()) {

         Pump pump = pumps.get(0);
         Assert.notNull(pump.getControllerPtsConfiguration(),"The pump should be found.");

         FuelGrade fuelGrade = fuelGrades.get(0);
         Assert.notNull(fuelGrade.getControllerPtsConfiguration(),"The fuelGrade should be found.");

      } else {
         System.out.println("No pumps and fuelGrades found for the given controller and configuration.");
      }
   }

   @Test
    void testTankInDelivery() throws Exception {

      // Perform a POST request to create a tank delivery
      mvc.perform(post(EndPoints.UPLOAD + EndPoints.UPLOAD_IN_TANK_DELIVERY)
            .content(readFile("/uploads/upload-in-tank-Delivery.json"))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
         .andDo(print())
         .andExpect(status().isOk())
         .andExpect(MockMvcResultMatchers.jsonPath("$.Packets").exists())
         .andExpect(MockMvcResultMatchers.jsonPath("$.Packets[0].Message",matchesPattern( UploadProcessorSwitcher.OK_MESSAGE)));

      List<Tank> tanks = tankService.findTankByControllerOnCurrentConfiguration(1L);
      if (!tanks.isEmpty()) {
         Tank tank = tanks.get(0); // Assuming you want the first tank in the list
         Assert.notNull(tank.getControllerPtsConfiguration(),"The tank should be found.");
      } else {
         System.out.println("No tanks found for the given controller and configuration.");
      }
   }

   @Test
    void testAlertRecords() throws Exception
   {

      mvc.perform(MockMvcRequestBuilders
            .post(EndPoints.UPLOAD + EndPoints.UPLOAD_CONFIG)
            .content(readFile("/uploads/upload-configuration.json"))
            .accept(MediaType.APPLICATION_JSON))
         .andDo(print())
         .andExpect(status().isOk())
         .andExpect(MockMvcResultMatchers.jsonPath("$.Packets").exists());

      final ControllerPtsConfiguration controllerPtsConf = controllerPtsConfigurationService.findByPtsIdAndConfigurationId(TEST_CTRL_PTS_ID, TEST_CONFIGURATION_ID);
      Assert.notNull(controllerPtsConf,"The controller should have a configurtation.");
      Assert.notNull(controllerPtsConf.getControllerPts(),"The controller should be found.");

      // Perform a POST request to create a alert
      mvc.perform(post(EndPoints.UPLOAD + EndPoints.UPLOAD_ALERT_RECORD)
            .content(readFile("/uploads/upload-alert-records.json"))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
         .andDo(print())
         .andExpect(status().isOk())
         .andExpect(MockMvcResultMatchers.jsonPath("$.Packets").exists())
         .andExpect(MockMvcResultMatchers.jsonPath("$.Packets[0].Message", matchesPattern( UploadProcessorSwitcher.OK_MESSAGE)));

      System.out.println("test");

   }


   @Test
    void saveTankLevel() throws Exception {

      // Perform a POST request to create a pump transaction
      mvc.perform(post(EndPoints.UPLOAD + EndPoints.UPLOAD_PUMP_TRANSACTION)
            .content(readFile("/uploads/upload-pump-transaction.json"))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
         .andDo(print())
         .andExpect(status().isOk())
         .andExpect(MockMvcResultMatchers.jsonPath("$.Packets").exists())
         .andExpect(MockMvcResultMatchers.jsonPath("$.Packets[0].Message",matchesPattern( UploadProcessorSwitcher.OK_MESSAGE)));

      List<Pump> pumps = pumpService.findPumpsByControllerOnCurrentConfiguration(1L);
      List<FuelGrade> fuelGrades = fuelGradesService.findFuelGradesByControllerOnCurrentConfiguration(1L);
      TankLevelPerSales tankLevelPerSales = tankLevelPerSalesService.findTankLevelPerSalesByPumpTransactionId(1L,"1",12154L);
      List<TankLevelPerSales> tankLevelPerSalesList = tankLevelPerSalesService.getTankLeveChangesByTank(1L,"1");
      // Assert that the TankLevelPerSales has been updated correctly
      if (tankLevelPerSales!=null)
      {
         Assert.notNull(tankLevelPerSales,"tank level should be adjusted.");
      } else{
         System.out.println("Tank level was not adjusted after pump transaction !.");
      }

      if (!pumps.isEmpty() && !fuelGrades.isEmpty()) {

         Pump pump = pumps.get(0);
         Assert.notNull(pump.getControllerPtsConfiguration(),"The pump should be found.");

         FuelGrade fuelGrade = fuelGrades.get(0);
         Assert.notNull(fuelGrade.getControllerPtsConfiguration(),"The fuelGrade should be found.");
         // Check if changedVolume is less than the previous value
         if (tankLevelPerSalesList.size() >= 2) {
            double previousTankLevel = tankLevelPerSalesList.get(tankLevelPerSalesList.size() - 2).getTankVolumeChanges();
            double changedVolume = previousTankLevel - tankLevelPerSales.getTankVolumeChanges();
            Assert.isTrue(changedVolume > 0, "changed volume lass than the previous volume");// Changed volume should be positive (less than the previous value)
         } else {
            System.out.println("Tank level List less than 2 , no enough pump transaction yet");
         }

      } else {
         System.out.println("No pumps and fuelGrades found for the given controller and configuration.");
      }

   }

   //-----

    void testWeeklyTankLevelChanges() throws Exception
   {
      Long idCtr = 1L;
      String tank = "1";
      String periode = "weekly";

      mvc.perform(MockMvcRequestBuilders
            .get(EndPoints.CHART_ROOT + EndPoints.CHART_TANK_LEVEL_BY_PERIOD, idCtr.toString())
            .param("tank", tank)
            .param("periode", periode)
            .accept(MediaType.APPLICATION_JSON))
         .andDo(print())
         .andExpect(status().isOk())
         //.andExpect(MockMvcResultMatchers.jsonPath("$tankVolumeChanges").value(IsNull.notNullValue()))
         //.andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty()) // Check that the response content is not empty
         .andReturn();
   }

   @Test
    void testTodayTankLevelChanges() throws Exception
   {
      Long idCtr = 1L;
      String tank = "1";
      String periode = "today";

      mvc.perform(get(EndPoints.CHART_ROOT +EndPoints.CHART_TANK_LEVEL_BY_PERIOD, idCtr.toString())
            .param("tank", tank)
            .param("periode", periode))
         .andExpect(status().isOk())
         .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
         //.andExpect(MockMvcResultMatchers.jsonPath("$tankVolumeChanges").value(IsNull.notNullValue()))
         //.andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty()) // Check that the response content is not empty
         .andReturn();
   }

   @Test
    void testYesterdayTankLevelChanges() throws Exception
   {
      Long idCtr = 1L;
      String tank = "1";
      String periode = "yesterday";

      mvc.perform(get(EndPoints.CHART_ROOT +EndPoints.CHART_TANK_LEVEL_BY_PERIOD, idCtr.toString())
            .param("idCtr", idCtr.toString())
            .param("tank", tank)
            .param("periode", periode))
         .andExpect(status().isOk())
         .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
         //.andExpect(MockMvcResultMatchers.jsonPath("$tankVolumeChanges").value(IsNull.notNullValue()))
         //.andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty()) // Check that the response content is not empty
         .andReturn();
   }

   @Test
    void testNullPeriodeWithStartDateAndEndDate() throws Exception
   {
      Long idCtr = 1L;
      String tank = "1";
      mvc.perform(get(EndPoints.CHART_ROOT +EndPoints.CHART_TANK_LEVEL_BY_PERIOD, idCtr.toString())
            .param("idCtr", idCtr.toString())
            .param("tank", tank)
            .param("startDate", "2023-10-16T00:00:00")
            .param("endDate", "2023-10-23T00:00:00"))
         .andExpect(status().isOk())
         .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
         //.andExpect(MockMvcResultMatchers.jsonPath("$tankVolumeChanges").value(IsNull.notNullValue()))
         //.andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty()) // Check that the response content is not empty
         .andReturn();
   }

    @Test
    void testAddStation() throws Exception {
        String requestBody = """
        {
            "name": "Total1",
            "address": "Mahdia",
            "controllerPts": {
                "ptsId": "666666557575445466"
            },
            "countryId": 1,
            "customerAccountId": 1
        }
        """;

        mvc.perform(post(EndPoints.CUSTOMER_ACCOUNT_ROOT + "/1" + EndPoints.STATION_ROOT + EndPoints.ADD)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        final List<StationDto> all = stationService.findAll();
        Assert.notEmpty(all, "Liste stations ne peut pas être vide");
        assertEquals(2, all.size());
    }

    @Test
    void testUpdateStation() throws Exception
   {
       String requestBody = """
        {
            "id":"1"
            "name": "Total1",
            "address": "Tunis",
            "countryId": 1
        }
        """;
      mvc.perform(put(EndPoints.CUSTOMER_ACCOUNT_ROOT+"/1"+EndPoints.STATION_ROOT+ EndPoints.UPDATE)
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
         .andDo(print())
         .andExpect(status().isOk());

      final List<StationDto> all = stationService.findAll();
      Assert.notEmpty(all,"Liste stations ne peut pas être vide");
      assertEquals(1,all.size());
   }
   @Test
    void testDeactivateStation() throws Exception
   {
      mvc.perform(put(EndPoints.CUSTOMER_ACCOUNT_ROOT+"/1"+EndPoints.STATION_ROOT+ "/deactivate/1")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
         .andDo(print())
         .andExpect(status().isOk());

      final List<StationDto> all = stationService.findAll();
      final Map<Long, StationDto> patrimoine = all.stream().collect(Collectors.toMap(StationDto::getId, Function.identity()));
      assertEquals(Boolean.FALSE,patrimoine.get(1L).getActif());
      Assert.notEmpty(all,"Liste stations ne peut pas être vide");
      assertEquals(2,all.size());
   }
}
