package com.teknokote.ess.core.repository;


import com.teknokote.ess.core.model.configuration.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository  extends JpaRepository<Tag, Long> {

    @Query("select t from Tag t where t.tags = ?1")
    Tag findTagsByTag(String tag);

    @Query("select t from Tag t where t.pump.idConf = :pump and t.nozzle.idConf= :nozzle ")
    List<Tag> findTag(@Param("pump")Long pump, @Param("nozzle")Long nozzle);

}
