package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.Tag;
import com.teknokote.ess.core.repository.TagRepository;
import com.teknokote.ess.core.service.base.AbstractEntityService;
import com.teknokote.pts.client.response.JsonPTSResponse;
import com.teknokote.pts.client.response.configuration.PTSTagsList;
import com.teknokote.pts.client.response.configuration.PTSTagsListResponsePacket;
import com.teknokote.pts.client.response.reader.TagInformation;
import com.teknokote.pts.client.response.reader.TagInformationResponsePacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TagServiceImpl extends AbstractEntityService<Tag, Long> implements TagService {
    private TagRepository tagRepository;

    public TagServiceImpl(TagRepository dao) {
        super(dao);
        this.tagRepository = dao;
    }
    @Override
    public Tag findTagsByTag(String tag) {
        return tagRepository.findTagsByTag(tag);
    }

    @Override
    public Tag addTag(Tag tag) {

        return tagRepository.save(tag);
    }

    @Override
    public Tag updateTag(Long id, Tag tag) {
        return tagRepository.findById(id)
                .map(p -> {
                    p.setTags(tag.getTags());
                    p.setName(tag.getName());
                    p.setValid(tag.getValid());
                    return tagRepository.save(tag);
                }).orElseThrow(() -> new RuntimeException("tag ne pas trouve !!"));
    }

    @Override
    public void addNewTag(JsonPTSResponse response) {

        response.getPackets().stream().map(responsePacket -> ((TagInformationResponsePacket) responsePacket).getData())
                .forEach(this::addTag);
    }

    private void addTag(TagInformation tagInformation) {
        Tag tag = new Tag();
        tag.setTags(tagInformation.getTag());
        tag.setName(tagInformation.getName());
        tag.setValid(tagInformation.getValid());
        tag.setPresent(tagInformation.getPresent());
        tagRepository.save(tag);
    }
    @Override
    public void addNewTags(JsonPTSResponse response) {

        response.getPackets().stream().map(responsePacket -> ((PTSTagsListResponsePacket) responsePacket).getData())
                .forEach(this::addTags);
    }

    private void addTags(List<PTSTagsList> ptsTagsLists) {
        for(PTSTagsList ptsTagsList:ptsTagsLists) {
            Tag tag = new Tag();
            tag.setTags(ptsTagsList.getTag());
            tag.setName(ptsTagsList.getName());
            tag.setValid(ptsTagsList.getValid());
            tagRepository.save(tag);
        }
    }
}
