package com.recitapp.recitapp_api.modules.notification.service.impl;

import com.recitapp.recitapp_api.common.exception.EntityNotFoundException;
import com.recitapp.recitapp_api.common.exception.RecitappException;
import com.recitapp.recitapp_api.modules.artist.entity.Artist;
import com.recitapp.recitapp_api.modules.artist.repository.ArtistFollowerRepository;
import com.recitapp.recitapp_api.modules.artist.repository.ArtistRepository;
import com.recitapp.recitapp_api.modules.artist.repository.MusicGenreRepository;
import com.recitapp.recitapp_api.modules.event.entity.Event;
import com.recitapp.recitapp_api.modules.event.repository.EventRepository;
import com.recitapp.recitapp_api.modules.notification.dto.*;
import com.recitapp.recitapp_api.modules.notification.entity.*;
import com.recitapp.recitapp_api.modules.notification.repository.*;
import com.recitapp.recitapp_api.modules.notification.service.NotificationService;
import com.recitapp.recitapp_api.modules.notification.entity.UserDeviceToken;
import com.recitapp.recitapp_api.modules.notification.service.EmailService;
import com.recitapp.recitapp_api.modules.notification.service.PushNotificationService;
import com.recitapp.recitapp_api.modules.notification.service.SmsService;
import com.recitapp.recitapp_api.modules.notification.service.WhatsAppService;
import com.recitapp.recitapp_api.modules.ticket.entity.Ticket;
import com.recitapp.recitapp_api.modules.ticket.repository.TicketRepository;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import com.recitapp.recitapp_api.modules.venue.entity.Venue;
import com.recitapp.recitapp_api.modules.venue.repository.VenueFollowerRepository;
import com.recitapp.recitapp_api.modules.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationHistoryRepository historyRepository;
    private final NotificationChannelRepository channelRepository;
    private final NotificationTypeRepository typeRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ArtistRepository artistRepository;
    private final VenueRepository venueRepository;
    private final ArtistFollowerRepository artistFollowerRepository;
    private final VenueFollowerRepository venueFollowerRepository;
    private final TicketRepository ticketRepository;
    private final MusicGenreRepository musicGenreRepository;
    
    // Notification delivery services
    private final EmailService emailService;
    private final PushNotificationService pushNotificationService;
    private final SmsService smsService;
    private final WhatsAppService whatsAppService;
    
    // Additional repositories for enhanced functionality
    private final UserDeviceTokenRepository deviceTokenRepository;

    // RAPP113935-114: Register notification preferences
    @Override
    public NotificationPreferenceDTO getUserNotificationPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + userId));

        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreference(user));

        return mapToPreferenceDTO(preference);
    }

    @Override
    @Transactional
    public NotificationPreferenceDTO updateUserNotificationPreferences(Long userId, NotificationPreferenceDTO preferencesDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + userId));

        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreference(user));

        updatePreferenceFields(preference, preferencesDTO);
        preferenceRepository.save(preference);

        return mapToPreferenceDTO(preference);
    }

    // RAPP113935-115: Emit new event alerts
    @Override
    @Transactional
    public void sendNewEventAlert(Long eventId, List<Long> followerUserIds) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Evento no encontrado con ID: " + eventId));

        NotificationType newEventType = getNotificationTypeByName("NUEVO_EVENTO");
        NotificationChannel pushChannel = getNotificationChannelByName("PUSH");

        for (Long userId : followerUserIds) {
            if (shouldReceiveEventNotification(userId)) {
                String content = String.format("¡Nuevo evento anunciado! %s en %s el %s",
                        event.getName(),
                        event.getVenue().getName(),
                        event.getStartDateTime().toLocalDate());

                createAndSaveNotification(userId, newEventType, pushChannel, content, event.getId(), null, null);
            }
        }

        log.info("Sent new event alerts for event {} to {} users", eventId, followerUserIds.size());
    }

    @Override
    @Transactional
    public void sendNewEventAlertToArtistFollowers(Long artistId, Long eventId) {
        List<Long> followerIds = artistFollowerRepository.findByUserIdOrderByFollowDateDesc(artistId)
                .stream()
                .map(follower -> follower.getUser().getId())
                .collect(Collectors.toList());

        if (!followerIds.isEmpty()) {
            sendNewEventAlert(eventId, followerIds);
        }
    }

    @Override
    @Transactional
    public void sendNewEventAlertToVenueFollowers(Long venueId, Long eventId) {
        List<Long> followerIds = venueFollowerRepository.findAllByUserId(venueId)
                .stream()
                .map(follower -> follower.getUser().getId())
                .collect(Collectors.toList());

        if (!followerIds.isEmpty()) {
            sendNewEventAlert(eventId, followerIds);
        }
    }

    // RAPP113935-116: Generate low ticket availability notifications
    @Override
    @Transactional
    public void sendLowAvailabilityAlert(Long eventId, Integer remainingTickets) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Evento no encontrado con ID: " + eventId));

        // Get users who have saved this event or follow the artist/venue
        List<Long> userIds = getUsersInterestedInEvent(eventId);

        NotificationType lowAvailabilityType = getNotificationTypeByName("POCAS_ENTRADAS");
        NotificationChannel pushChannel = getNotificationChannelByName("PUSH");

        String content = String.format("¡Pocas entradas disponibles! Solo quedan %d entradas para %s",
                remainingTickets, event.getName());

        for (Long userId : userIds) {
            if (shouldReceiveAvailabilityNotification(userId)) {
                createAndSaveNotification(userId, lowAvailabilityType, pushChannel, content, eventId, null, null);
            }
        }

        log.info("Sent low availability alerts for event {} to {} users", eventId, userIds.size());
    }

    @Override
    @Transactional
    public void checkAndSendLowAvailabilityAlerts() {
        // Find events that are in sale status
        List<Event> eventsInSale = eventRepository.findByFilters(
                null, null, null, null, "EN_VENTA", null, null, null);

        for (Event event : eventsInSale) {
            Long totalCapacity = Long.valueOf(event.getVenue().getTotalCapacity());
            Long soldTickets = ticketRepository.countSoldTicketsByEventId(event.getId());
            Long remainingTickets = totalCapacity - soldTickets;

            // Send alert if less than 10% of tickets remain
            if (remainingTickets > 0 && remainingTickets <= (totalCapacity * 0.1)) {
                sendLowAvailabilityAlert(event.getId(), remainingTickets.intValue());
            }
        }
    }

    // RAPP113935-117: Query notification history
    @Override
    public List<NotificationDTO> getUserNotificationHistory(Long userId) {
        validateUser(userId);
        List<NotificationHistory> notifications = historyRepository.findByUserIdOrderBySentAtDesc(userId);
        return notifications.stream().map(this::mapToNotificationDTO).collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getUserNotificationHistory(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        validateUser(userId);
        List<NotificationHistory> allNotifications = historyRepository.findByUserIdOrderBySentAtDesc(userId);

        return allNotifications.stream()
                .filter(n -> n.getSentAt().isAfter(startDate) && n.getSentAt().isBefore(endDate))
                .map(this::mapToNotificationDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        validateUser(userId);
        List<NotificationHistory> notifications = historyRepository.findByUserIdAndReadAtIsNullOrderBySentAtDesc(userId);
        return notifications.stream().map(this::mapToNotificationDTO).collect(Collectors.toList());
    }

    @Override
    public NotificationSummaryDTO getNotificationSummary(Long userId) {
        validateUser(userId);

        Long totalNotifications = (long) historyRepository.findByUserIdOrderBySentAtDesc(userId).size();
        Long unreadNotifications = historyRepository.countByUserIdAndReadAtIsNull(userId);

        List<NotificationHistory> recent = historyRepository.findRecentNotifications(userId,
                LocalDateTime.now().minusDays(7));

        LocalDateTime lastNotificationDate = recent.isEmpty() ? null : recent.get(0).getSentAt();

        List<NotificationDTO> recentDTOs = recent.stream()
                .limit(5)
                .map(this::mapToNotificationDTO)
                .collect(Collectors.toList());

        return NotificationSummaryDTO.builder()
                .userId(userId)
                .totalNotifications(totalNotifications)
                .unreadNotifications(unreadNotifications)
                .lastNotificationDate(lastNotificationDate)
                .recentNotifications(recentDTOs)
                .build();
    }

    @Override
    @Transactional
    public NotificationDTO markAsRead(Long notificationId) {
        NotificationHistory notification = historyRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada con ID: " + notificationId));

        notification.setReadAt(LocalDateTime.now());
        historyRepository.save(notification);

        return mapToNotificationDTO(notification);
    }

    @Override
    @Transactional
    public void markMultipleAsRead(Long userId, List<Long> notificationIds) {
        for (Long notificationId : notificationIds) {
            Optional<NotificationHistory> notification = historyRepository.findById(notificationId);
            if (notification.isPresent() && notification.get().getUser().getId().equals(userId)) {
                notification.get().setReadAt(LocalDateTime.now());
                historyRepository.save(notification.get());
            }
        }
    }

    // RAPP113935-118: Register cancellation/modification notifications
    @Override
    @Transactional
    public void sendEventCancellationNotification(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Evento no encontrado con ID: " + eventId));

        List<Long> affectedUserIds = getUsersWithTicketsForEvent(eventId);

        NotificationType cancellationType = getNotificationTypeByName("CANCELACION");
        NotificationChannel emailChannel = getNotificationChannelByName("EMAIL");

        String content = String.format("El evento %s ha sido cancelado. Se procesará el reembolso automáticamente.",
                event.getName());

        for (Long userId : affectedUserIds) {
            if (shouldReceiveReminderEmails(userId)) {
                createAndSaveNotification(userId, cancellationType, emailChannel, content, eventId, null, null);
            }
        }

        log.info("Sent cancellation notifications for event {} to {} users", eventId, affectedUserIds.size());
    }

    @Override
    @Transactional
    public void sendEventModificationNotification(Long eventId, String changeDescription) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Evento no encontrado con ID: " + eventId));

        List<Long> affectedUserIds = getUsersWithTicketsForEvent(eventId);

        // Usar el nuevo tipo MODIFICACION en lugar de CANCELACION
        NotificationType modificationType = getNotificationTypeByName("MODIFICACION");
        NotificationChannel emailChannel = getNotificationChannelByName("EMAIL");

        String content = String.format("El evento %s ha sido modificado: %s",
                event.getName(), changeDescription);

        for (Long userId : affectedUserIds) {
            if (shouldReceiveReminderEmails(userId)) {
                createAndSaveNotification(userId, modificationType, emailChannel, content, eventId, null, null);
            }
        }

        log.info("Sent modification notifications for event {} to {} users", eventId, affectedUserIds.size());
    }

    @Override
    @Transactional
    public void sendTicketCancellationNotification(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado con ID: " + ticketId));

        NotificationType cancellationType = getNotificationTypeByName("CANCELACION");
        NotificationChannel emailChannel = getNotificationChannelByName("EMAIL");

        String content = String.format("Tu entrada para %s ha sido cancelada. Se procesará el reembolso.",
                ticket.getEvent().getName());

        if (shouldReceiveReminderEmails(ticket.getUser().getId())) {
            createAndSaveNotification(ticket.getUser().getId(), cancellationType, emailChannel,
                    content, ticket.getEvent().getId(), null, null);
        }
    }

    // RAPP113935-119: Send recommendations based on preferences
    @Override
    @Transactional
    public void sendPersonalizedRecommendations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + userId));

        if (!shouldReceiveWeeklyNewsletter(userId)) {
            return;
        }

        // Get user's followed artists genres
        List<String> userGenres = getUserPreferredGenres(userId);

        // Find upcoming events in those genres
        List<Event> recommendedEvents = findRecommendedEvents(userGenres);

        if (!recommendedEvents.isEmpty()) {
            NotificationType recommendationType = getNotificationTypeByName("RECOMENDACION");
            NotificationChannel emailChannel = getNotificationChannelByName("EMAIL");

            StringBuilder content = new StringBuilder("Eventos recomendados para ti: ");
            recommendedEvents.stream()
                    .limit(3)
                    .forEach(event -> content.append(event.getName()).append(", "));

            createAndSaveNotification(userId, recommendationType, emailChannel,
                    content.toString(), null, null, null);
        }
    }

    @Override
    @Transactional
    public void sendWeeklyRecommendations() {
        List<User> activeUsers = userRepository.findAll().stream()
                .filter(user -> user.getActive())
                .collect(Collectors.toList());

        for (User user : activeUsers) {
            try {
                sendPersonalizedRecommendations(user.getId());
            } catch (Exception e) {
                log.error("Error sending recommendations to user {}: {}", user.getId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void sendGenreBasedRecommendations(Long userId, List<String> preferredGenres) {
        validateUser(userId);

        List<Event> recommendedEvents = findRecommendedEvents(preferredGenres);

        if (!recommendedEvents.isEmpty() && shouldReceiveWeeklyNewsletter(userId)) {
            NotificationType recommendationType = getNotificationTypeByName("RECOMENDACION");
            NotificationChannel emailChannel = getNotificationChannelByName("EMAIL");

            String content = String.format("Eventos de %s que te pueden interesar: %s",
                    String.join(", ", preferredGenres),
                    recommendedEvents.stream()
                            .limit(3)
                            .map(Event::getName)
                            .collect(Collectors.joining(", ")));

            createAndSaveNotification(userId, recommendationType, emailChannel, content, null, null, null);
        }
    }

    // RAPP113935-120: Update notification channels
    @Override
    public List<NotificationChannelDTO> getAllNotificationChannels() {
        return channelRepository.findAll().stream()
                .map(this::mapToChannelDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationChannelDTO createNotificationChannel(NotificationChannelDTO channelDTO) {
        if (channelRepository.existsByName(channelDTO.getName())) {
            throw new RecitappException("Canal de notificación ya existe: " + channelDTO.getName());
        }

        NotificationChannel channel = new NotificationChannel();
        channel.setName(channelDTO.getName());
        channel.setDescription(channelDTO.getDescription());
        channel.setActive(channelDTO.getActive() != null ? channelDTO.getActive() : true);

        NotificationChannel saved = channelRepository.save(channel);
        return mapToChannelDTO(saved);
    }

    @Override
    @Transactional
    public NotificationChannelDTO updateNotificationChannel(Long channelId, NotificationChannelDTO channelDTO) {
        NotificationChannel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new EntityNotFoundException("Canal no encontrado con ID: " + channelId));

        if (channelDTO.getName() != null) {
            channel.setName(channelDTO.getName());
        }
        if (channelDTO.getDescription() != null) {
            channel.setDescription(channelDTO.getDescription());
        }
        if (channelDTO.getActive() != null) {
            channel.setActive(channelDTO.getActive());
        }

        NotificationChannel updated = channelRepository.save(channel);
        return mapToChannelDTO(updated);
    }

    @Override
    @Transactional
    public void deleteNotificationChannel(Long channelId) {
        if (!channelRepository.existsById(channelId)) {
            throw new EntityNotFoundException("Canal no encontrado con ID: " + channelId);
        }
        channelRepository.deleteById(channelId);
    }

    @Override
    public List<NotificationChannelDTO> getActiveNotificationChannels() {
        return channelRepository.findByActiveTrue().stream()
                .map(this::mapToChannelDTO)
                .collect(Collectors.toList());
    }

    // Additional utility methods
    @Override
    @Transactional
    public NotificationDTO createNotification(NotificationCreateDTO createDTO) {
        NotificationType type = getNotificationTypeByName(createDTO.getTypeName());
        NotificationChannel channel = getNotificationChannelByName(createDTO.getChannelName());

        NotificationHistory notification = createAndSaveNotification(
                createDTO.getUserId(), type, channel, createDTO.getContent(),
                createDTO.getRelatedEventId(), createDTO.getRelatedArtistId(), createDTO.getRelatedVenueId());

        return mapToNotificationDTO(notification);
    }

    @Override
    @Transactional
    public void sendBulkNotification(BulkNotificationDTO bulkDTO) {
        NotificationType type = getNotificationTypeByName(bulkDTO.getTypeName());
        NotificationChannel channel = getNotificationChannelByName(bulkDTO.getChannelName());

        for (Long userId : bulkDTO.getUserIds()) {
            try {
                createAndSaveNotification(userId, type, channel, bulkDTO.getContent(),
                        bulkDTO.getRelatedEventId(), bulkDTO.getRelatedArtistId(), bulkDTO.getRelatedVenueId());
            } catch (Exception e) {
                log.error("Error sending notification to user {}: {}", userId, e.getMessage());
            }
        }
    }

    @Override
    public List<NotificationDTO> getNotificationsByEvent(Long eventId) {
        List<NotificationHistory> notifications = historyRepository.findByRelatedEventIdOrderBySentAtDesc(eventId);
        return notifications.stream().map(this::mapToNotificationDTO).collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getNotificationsByArtist(Long artistId) {
        List<NotificationHistory> notifications = historyRepository.findByRelatedArtistIdOrderBySentAtDesc(artistId);
        return notifications.stream().map(this::mapToNotificationDTO).collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getNotificationsByVenue(Long venueId) {
        List<NotificationHistory> notifications = historyRepository.findByRelatedVenueIdOrderBySentAtDesc(venueId);
        return notifications.stream().map(this::mapToNotificationDTO).collect(Collectors.toList());
    }

    @Override
    public List<NotificationTypeDTO> getAllNotificationTypes() {
        return typeRepository.findAll().stream()
                .map(this::mapToTypeDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationTypeDTO createNotificationType(NotificationTypeDTO typeDTO) {
        if (typeRepository.existsByName(typeDTO.getName())) {
            throw new RecitappException("Tipo de notificación ya existe: " + typeDTO.getName());
        }

        NotificationType type = new NotificationType();
        type.setName(typeDTO.getName());
        type.setDescription(typeDTO.getDescription());
        type.setTemplate(typeDTO.getTemplate());

        NotificationType saved = typeRepository.save(type);
        return mapToTypeDTO(saved);
    }

    @Override
    @Transactional
    public NotificationTypeDTO updateNotificationType(Long typeId, NotificationTypeDTO typeDTO) {
        NotificationType type = typeRepository.findById(typeId)
                .orElseThrow(() -> new EntityNotFoundException("Tipo no encontrado con ID: " + typeId));

        if (typeDTO.getName() != null) {
            type.setName(typeDTO.getName());
        }
        if (typeDTO.getDescription() != null) {
            type.setDescription(typeDTO.getDescription());
        }
        if (typeDTO.getTemplate() != null) {
            type.setTemplate(typeDTO.getTemplate());
        }

        NotificationType updated = typeRepository.save(type);
        return mapToTypeDTO(updated);
    }

    // Private helper methods
    private NotificationPreference createDefaultPreference(User user) {
        NotificationPreference preference = new NotificationPreference();
        preference.setUser(user);
        return preferenceRepository.save(preference);
    }

    private void updatePreferenceFields(NotificationPreference preference, NotificationPreferenceDTO dto) {
        if (dto.getReceiveReminderEmails() != null) {
            preference.setReceiveReminderEmails(dto.getReceiveReminderEmails());
        }
        if (dto.getReceiveEventPush() != null) {
            preference.setReceiveEventPush(dto.getReceiveEventPush());
        }
        if (dto.getReceiveArtistPush() != null) {
            preference.setReceiveArtistPush(dto.getReceiveArtistPush());
        }
        if (dto.getReceiveAvailabilityPush() != null) {
            preference.setReceiveAvailabilityPush(dto.getReceiveAvailabilityPush());
        }
        if (dto.getReceiveWeeklyNewsletter() != null) {
            preference.setReceiveWeeklyNewsletter(dto.getReceiveWeeklyNewsletter());
        }
    }

    private NotificationHistory createAndSaveNotification(Long userId, NotificationType type,
                                                          NotificationChannel channel, String content, Long eventId, Long artistId, Long venueId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + userId));

        NotificationHistory notification = new NotificationHistory();
        notification.setUser(user);
        notification.setType(type);
        notification.setChannel(channel);
        notification.setContent(content);

        if (eventId != null) {
            Event event = eventRepository.findById(eventId).orElse(null);
            notification.setRelatedEvent(event);
        }
        if (artistId != null) {
            Artist artist = artistRepository.findById(artistId).orElse(null);
            notification.setRelatedArtist(artist);
        }
        if (venueId != null) {
            Venue venue = venueRepository.findById(venueId).orElse(null);
            notification.setRelatedVenue(venue);
        }

        // Guardar en historial primero
        NotificationHistory savedNotification = historyRepository.save(notification);
        
        // Enviar la notificación según el canal
        try {
            sendNotificationByChannel(user, channel, type, content, eventId, artistId, venueId);
        } catch (Exception e) {
            log.error("Failed to send notification via {}: {}", channel.getName(), e.getMessage());
            // No lanzamos excepción para no afectar el flujo principal
        }

        return savedNotification;
    }

    private void sendNotificationByChannel(User user, NotificationChannel channel, NotificationType type, 
                                         String content, Long eventId, Long artistId, Long venueId) {
        String channelName = channel.getName().toUpperCase();
        
        switch (channelName) {
            case "EMAIL":
                sendEmailNotification(user, type, content, eventId, artistId, venueId);
                break;
            case "PUSH":
                sendPushNotification(user, type, content, eventId, artistId, venueId);
                break;
            case "SMS":
                sendSmsNotification(user, type, content, eventId, artistId, venueId);
                break;
            case "WHATSAPP":
                sendWhatsAppNotification(user, type, content, eventId, artistId, venueId);
                break;
            default:
                log.warn("Unknown notification channel: {}", channelName);
        }
    }

    private void sendEmailNotification(User user, NotificationType type, String content, 
                                     Long eventId, Long artistId, Long venueId) {
        try {
            String subject = generateEmailSubject(type, eventId, artistId, venueId);
            
            // Si hay plantilla HTML disponible, usar template email
            if (type.getTemplate() != null && !type.getTemplate().isEmpty()) {
                Map<String, Object> variables = buildTemplateVariables(user, eventId, artistId, venueId);
                emailService.sendTemplateEmail(user.getEmail(), subject, type.getTemplate(), variables);
            } else {
                // Enviar email simple
                emailService.sendSimpleEmail(user.getEmail(), subject, content);
            }
            
            log.info("Email notification sent to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email notification to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private void sendPushNotification(User user, NotificationType type, String content, 
                                    Long eventId, Long artistId, Long venueId) {
        try {
            // Obtener el device token del usuario (esto requeriría una tabla adicional)
            String deviceToken = getUserDeviceToken(user.getId());
            if (deviceToken == null || deviceToken.isEmpty()) {
                log.warn("No device token found for user: {}", user.getId());
                return;
            }

            String title = generatePushTitle(type, eventId, artistId, venueId);
            Map<String, String> data = buildPushData(eventId, artistId, venueId, type.getName());
            
            pushNotificationService.sendToDevice(deviceToken, title, content, data);
            log.info("Push notification sent to user: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to send push notification to user {}: {}", user.getId(), e.getMessage());
        }
    }

    private void sendSmsNotification(User user, NotificationType type, String content, 
                                   Long eventId, Long artistId, Long venueId) {
        try {
            String phoneNumber = getUserPhoneNumber(user.getId());
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                log.warn("No phone number found for user: {}", user.getId());
                return;
            }

            // Usar plantilla SMS si está disponible
            if (type.getTemplate() != null && !type.getTemplate().isEmpty()) {
                String[] parameters = buildSmsParameters(eventId, artistId, venueId);
                smsService.sendTemplateSms(phoneNumber, type.getTemplate(), parameters);
            } else {
                smsService.sendSms(phoneNumber, content);
            }
            
            log.info("SMS notification sent to user: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to send SMS notification to user {}: {}", user.getId(), e.getMessage());
        }
    }

    private void sendWhatsAppNotification(User user, NotificationType type, String content, 
                                        Long eventId, Long artistId, Long venueId) {
        try {
            String phoneNumber = getUserPhoneNumber(user.getId());
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                log.warn("No phone number found for user: {}", user.getId());
                return;
            }

            // Usar plantilla WhatsApp si está disponible
            if (type.getTemplate() != null && !type.getTemplate().isEmpty()) {
                Map<String, String> parameters = buildWhatsAppParameters(eventId, artistId, venueId);
                whatsAppService.sendTemplateMessage(phoneNumber, type.getTemplate(), parameters);
            } else {
                whatsAppService.sendWhatsAppMessage(phoneNumber, content);
            }
            
            log.info("WhatsApp notification sent to user: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to send WhatsApp notification to user {}: {}", user.getId(), e.getMessage());
        }
    }

    private String generateEmailSubject(NotificationType type, Long eventId, Long artistId, Long venueId) {
        String typeName = type.getName();
        
        switch (typeName) {
            case "NUEVO_EVENTO":
                return "🎵 ¡Nuevo evento disponible en RecitApp!";
            case "POCAS_ENTRADAS":
                return "⚠️ ¡Pocas entradas disponibles!";
            case "CANCELACION":
                return "❌ Evento cancelado - Información importante";
            case "MODIFICACION":
                return "📝 Cambios en tu evento";
            case "RECORDATORIO":
                return "🔔 Recordatorio de evento";
            case "RECOMENDACION":
                return "🎯 Eventos recomendados para ti";
            default:
                return "Notificación de RecitApp";
        }
    }

    private String generatePushTitle(NotificationType type, Long eventId, Long artistId, Long venueId) {
        String typeName = type.getName();
        
        switch (typeName) {
            case "NUEVO_EVENTO":
                return "¡Nuevo evento!";
            case "POCAS_ENTRADAS":
                return "¡Pocas entradas!";
            case "CANCELACION":
                return "Evento cancelado";
            case "MODIFICACION":
                return "Evento modificado";
            case "RECORDATORIO":
                return "Recordatorio";
            case "RECOMENDACION":
                return "Recomendaciones";
            default:
                return "RecitApp";
        }
    }

    private Map<String, Object> buildTemplateVariables(User user, Long eventId, Long artistId, Long venueId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", user.getFirstName());
        variables.put("userEmail", user.getEmail());
        
        if (eventId != null) {
            Event event = eventRepository.findById(eventId).orElse(null);
            if (event != null) {
                variables.put("eventName", event.getName());
                variables.put("eventDate", event.getStartDateTime());
                variables.put("venueName", event.getVenue().getName());
            }
        }
        
        if (artistId != null) {
            Artist artist = artistRepository.findById(artistId).orElse(null);
            if (artist != null) {
                variables.put("artistName", artist.getName());
            }
        }
        
        if (venueId != null) {
            Venue venue = venueRepository.findById(venueId).orElse(null);
            if (venue != null) {
                variables.put("venueName", venue.getName());
            }
        }
        
        return variables;
    }

    private Map<String, String> buildPushData(Long eventId, Long artistId, Long venueId, String notificationType) {
        Map<String, String> data = new HashMap<>();
        data.put("type", notificationType);
        
        if (eventId != null) {
            data.put("eventId", eventId.toString());
        }
        if (artistId != null) {
            data.put("artistId", artistId.toString());
        }
        if (venueId != null) {
            data.put("venueId", venueId.toString());
        }
        
        return data;
    }

    private String[] buildSmsParameters(Long eventId, Long artistId, Long venueId) {
        List<String> parameters = new ArrayList<>();
        
        if (eventId != null) {
            Event event = eventRepository.findById(eventId).orElse(null);
            if (event != null) {
                parameters.add(event.getName());
                parameters.add(event.getVenue().getName());
            }
        }
        
        if (artistId != null) {
            Artist artist = artistRepository.findById(artistId).orElse(null);
            if (artist != null) {
                parameters.add(artist.getName());
            }
        }
        
        return parameters.toArray(new String[0]);
    }

    private Map<String, String> buildWhatsAppParameters(Long eventId, Long artistId, Long venueId) {
        Map<String, String> parameters = new HashMap<>();
        
        if (eventId != null) {
            Event event = eventRepository.findById(eventId).orElse(null);
            if (event != null) {
                parameters.put("eventName", event.getName());
                parameters.put("eventDate", event.getStartDateTime().toString());
                parameters.put("venueName", event.getVenue().getName());
                if (event.getStartDateTime() != null) {
                    parameters.put("eventTime", event.getStartDateTime().toLocalTime().toString());
                }
            }
        }
        
        if (artistId != null) {
            Artist artist = artistRepository.findById(artistId).orElse(null);
            if (artist != null) {
                parameters.put("artistName", artist.getName());
            }
        }
        
        if (venueId != null) {
            Venue venue = venueRepository.findById(venueId).orElse(null);
            if (venue != null) {
                parameters.put("venueName", venue.getName());
            }
        }
        
        return parameters;
    }

    private String getUserDeviceToken(Long userId) {
        // Buscar el token más reciente del usuario (prioridad Android > iOS > Web)
        Optional<UserDeviceToken> androidToken = deviceTokenRepository
            .findLatestTokenByUserAndDeviceType(userId, UserDeviceToken.DeviceType.ANDROID);
        if (androidToken.isPresent()) {
            return androidToken.get().getDeviceToken();
        }
        
        Optional<UserDeviceToken> iosToken = deviceTokenRepository
            .findLatestTokenByUserAndDeviceType(userId, UserDeviceToken.DeviceType.IOS);
        if (iosToken.isPresent()) {
            return iosToken.get().getDeviceToken();
        }
        
        Optional<UserDeviceToken> webToken = deviceTokenRepository
            .findLatestTokenByUserAndDeviceType(userId, UserDeviceToken.DeviceType.WEB);
        if (webToken.isPresent()) {
            return webToken.get().getDeviceToken();
        }
        
        return null;
    }

    private String getUserPhoneNumber(Long userId) {
        // TODO: Agregar campo phone number a la entidad User
        // Por ahora retornamos null
        return null;
    }

    private NotificationType getNotificationTypeByName(String name) {
        return typeRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de notificación no encontrado: " + name));
    }

    private NotificationChannel getNotificationChannelByName(String name) {
        return channelRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Canal de notificación no encontrado: " + name));
    }

    private boolean shouldReceiveEventNotification(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .map(pref -> pref.getReceiveEventPush() != null ? pref.getReceiveEventPush() : true)
                .orElse(true);
    }

    private boolean shouldReceiveAvailabilityNotification(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .map(pref -> pref.getReceiveAvailabilityPush() != null ? pref.getReceiveAvailabilityPush() : true)
                .orElse(true);
    }

    private boolean shouldReceiveReminderEmails(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .map(pref -> pref.getReceiveReminderEmails() != null ? pref.getReceiveReminderEmails() : true)
                .orElse(true);
    }

    private boolean shouldReceiveWeeklyNewsletter(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .map(pref -> pref.getReceiveWeeklyNewsletter() != null ? pref.getReceiveWeeklyNewsletter() : true)
                .orElse(true);
    }

    private List<Long> getUsersInterestedInEvent(Long eventId) {
        // This would need to be implemented based on saved events functionality
        // For now, return empty list
        return new ArrayList<>();
    }

    private List<Long> getUsersWithTicketsForEvent(Long eventId) {
        List<Ticket> tickets = ticketRepository.findByEventId(eventId);
        return tickets.stream()
                .map(ticket -> ticket.getUser().getId())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> getUserPreferredGenres(Long userId) {
        // This would need to analyze user's followed artists and their genres
        // For now, return empty list
        return new ArrayList<>();
    }

    private List<Event> findRecommendedEvents(List<String> genres) {
        // This would need to find events based on genres
        // For now, return empty list
        return new ArrayList<>();
    }

    private void validateUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Usuario no encontrado con ID: " + userId);
        }
    }

    // Mapping methods
    private NotificationPreferenceDTO mapToPreferenceDTO(NotificationPreference preference) {
        return new NotificationPreferenceDTO(
                preference.getReceiveReminderEmails(),
                preference.getReceiveEventPush(),
                preference.getReceiveArtistPush(),
                preference.getReceiveAvailabilityPush(),
                preference.getReceiveWeeklyNewsletter()
        );
    }

    private NotificationDTO mapToNotificationDTO(NotificationHistory notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .typeName(notification.getType().getName())
                .channelName(notification.getChannel().getName())
                .content(notification.getContent())
                .sentAt(notification.getSentAt())
                .readAt(notification.getReadAt())
                .isRead(notification.getReadAt() != null)
                .relatedEventId(notification.getRelatedEvent() != null ? notification.getRelatedEvent().getId() : null)
                .relatedEventName(notification.getRelatedEvent() != null ? notification.getRelatedEvent().getName() : null)
                .relatedArtistId(notification.getRelatedArtist() != null ? notification.getRelatedArtist().getId() : null)
                .relatedArtistName(notification.getRelatedArtist() != null ? notification.getRelatedArtist().getName() : null)
                .relatedVenueId(notification.getRelatedVenue() != null ? notification.getRelatedVenue().getId() : null)
                .relatedVenueName(notification.getRelatedVenue() != null ? notification.getRelatedVenue().getName() : null)
                .build();
    }

    private NotificationChannelDTO mapToChannelDTO(NotificationChannel channel) {
        return NotificationChannelDTO.builder()
                .id(channel.getId())
                .name(channel.getName())
                .description(channel.getDescription())
                .active(channel.getActive())
                .build();
    }

    private NotificationTypeDTO mapToTypeDTO(NotificationType type) {
        return NotificationTypeDTO.builder()
                .id(type.getId())
                .name(type.getName())
                .description(type.getDescription())
                .template(type.getTemplate())
                .build();
    }
}