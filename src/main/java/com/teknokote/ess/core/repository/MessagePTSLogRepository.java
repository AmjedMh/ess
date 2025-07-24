package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.exchanges.EnumMessageOrigin;
import com.teknokote.ess.core.model.exchanges.MessagePtsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessagePTSLogRepository extends JpaRepository<MessagePtsLog,Long> {

   @Query("select m.id from MessagePtsLog m where m.ptsId = :ptsId and m.messageOrigin = :origin and m.messageDate between :startDate AND :endDate and m.message like '%\"Type\":\"UploadPumpTransaction\"%'")
   List<Long> findFailedTransactions(String ptsId, EnumMessageOrigin origin, LocalDateTime startDate,LocalDateTime endDate);

   @Query(value = """
    SELECT *
    FROM message_pts_log
    WHERE message LIKE '%"Type":"UploadTankMeasurement"%'
    AND pts_id = :ptsId
    AND message_origin = 'CTRL' 
    AND to_timestamp(
            substring(message FROM '"DateTime":"([0-9\\-T:]+)"') 
          , 'YYYY-MM-DD"T"HH24:MI:SS'
      ) BETWEEN :startDate AND :endDate
    ORDER BY to_timestamp(
            substring(message FROM '"DateTime":"([0-9\\-T:]+)"') 
          , 'YYYY-MM-DD"T"HH24:MI:SS'
      ) ASC
    """, nativeQuery = true)
   List<MessagePtsLog> findMeasurementByMessageTypeAndDateRange(
           @Param("startDate") LocalDateTime startDate,
           @Param("endDate") LocalDateTime endDate,
           @Param("ptsId") String ptsId
   );
   @Query("select m from MessagePtsLog m " +
           "where m.ptsId = :ptsId " +
           "and m.messageOrigin = 'CTRL' " +
           "and m.messageDate between :startDate and :endDate " +
           "order by m.messageDate asc")
   List<MessagePtsLog> findMessagesByDate(LocalDateTime startDate, LocalDateTime endDate, String ptsId);

}
