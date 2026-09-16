package com.aitutor.intelligence;

import com.aitutor.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningEventServiceTest {
    @Mock LearningEventRepository repository;
    @Mock User user;

    @Test
    void dedupeKeyReturnsExistingEventWithoutCreatingAnotherOne() {
        var service = new LearningEventService(repository);
        var existing = new LearningEvent();
        when(user.getId()).thenReturn(42L);
        when(repository.findByUserIdAndDedupeKey(42L, "LESSON:42:1")).thenReturn(Optional.of(existing));

        var result = service.record(user, "LESSON_COMPLETED", "Java", "Lesson 1", 100, 0, "done", "LESSON:42:1");

        assertSame(existing, result);
        verify(repository, never()).insertIfAbsent(anyLong(), anyString(), any(), any(), any(), any(), any(), anyString(), any());
    }
}
