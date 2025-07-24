package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.model.configuration.Tag;
import com.teknokote.ess.core.repository.TagRepository;
import com.teknokote.pts.client.response.JsonPTSResponse;
import com.teknokote.pts.client.response.configuration.PTSTagsList;
import com.teknokote.pts.client.response.configuration.PTSTagsListResponsePacket;
import com.teknokote.pts.client.response.reader.TagInformation;
import com.teknokote.pts.client.response.reader.TagInformationResponsePacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    private Tag tag;

    @BeforeEach
    void setup() {
        tag = new Tag();
        tag.setId(1L);
        tag.setTags("TAG_1");
        tag.setName("Test Tag");
        tag.setValid(true);
        tag.setPresent(true);
    }

    @Test
    void testFindTagsByTag() {
        when(tagRepository.findTagsByTag("TAG_1")).thenReturn(tag);
        Tag found = tagService.findTagsByTag("TAG_1");
        assertNotNull(found);
        assertEquals("Test Tag", found.getName());
    }

    @Test
    void testAddTag() {
        when(tagRepository.save(tag)).thenReturn(tag);
        Tag saved = tagService.addTag(tag);
        assertEquals(tag, saved);
    }

    @Test
    void testUpdateTag_success() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.save(any(Tag.class))).thenReturn(tag);

        Tag update = new Tag();
        update.setTags("UPDATED_TAG");
        update.setName("Updated Name");
        update.setValid(false);

        Tag updated = tagService.updateTag(1L, update);
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    void testUpdateTag_notFound() {
        when(tagRepository.findById(2L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> tagService.updateTag(2L, tag));
        assertEquals("tag ne pas trouve !!", exception.getMessage());
    }

    @Test
    void testAddNewTag() {
        TagInformation tagInfo = new TagInformation();
        tagInfo.setTag("TAG_A");
        tagInfo.setName("Name A");
        tagInfo.setValid(true);
        tagInfo.setPresent(true);

        TagInformationResponsePacket packet = mock(TagInformationResponsePacket.class);
        when(packet.getData()).thenReturn(tagInfo);

        JsonPTSResponse response = mock(JsonPTSResponse.class);
        when(response.getPackets()).thenReturn(List.of(packet));

        tagService.addNewTag(response);

        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    void testAddNewTags() {
        PTSTagsList ptsTag = new PTSTagsList();
        ptsTag.setTag("TAG_B");
        ptsTag.setName("Name B");
        ptsTag.setValid(true);

        PTSTagsListResponsePacket packet = mock(PTSTagsListResponsePacket.class);
        when(packet.getData()).thenReturn(List.of(ptsTag));

        JsonPTSResponse response = mock(JsonPTSResponse.class);
        when(response.getPackets()).thenReturn(List.of(packet));

        tagService.addNewTags(response);

        verify(tagRepository, times(1)).save(any(Tag.class));
    }
}
