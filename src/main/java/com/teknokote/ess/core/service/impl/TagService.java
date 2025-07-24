package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.Tag;
import com.teknokote.ess.core.service.base.EntityService;
import com.teknokote.pts.client.response.JsonPTSResponse;

public interface TagService extends EntityService<Tag, Long>
{

    Tag findTagsByTag(String tag);
    Tag addTag(Tag tag);
    Tag updateTag(Long id, Tag tag);
    void addNewTag(JsonPTSResponse tagInformation);

    void addNewTags(JsonPTSResponse response);
}
