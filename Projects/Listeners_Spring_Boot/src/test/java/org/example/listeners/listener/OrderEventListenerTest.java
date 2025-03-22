package org.example.listeners.listener;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.example.listeners.enums.OrderEventTypeEnum;
import org.example.listeners.enums.OrderStatusEnum;
import org.example.listeners.event.OrderEvent;
import org.example.listeners.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

    private OrderEventListener orderEventListener;
    private Order testOrder;
    private OrderEvent orderCreatedEvent;
    private OrderEvent orderCompletedEvent;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        /*
        Вместо создания этого объекта напрямую мы могли повесить
        аннотацию @InjectMocks на поле private OrderEventListener orderEventListener;
        но поскольку у нас нет других полей помеченных аннотацией @Mock так что значит
        внедрять нечего, поэтому мы создаем просто поле вручную вместо вешания этой аннотации на переменную
         */
        orderEventListener = new OrderEventListener();
        testOrder = new Order(1L, OrderStatusEnum.NEW, 100.0);
        orderCreatedEvent = new OrderEvent(this, testOrder, OrderEventTypeEnum.CREATED);
        orderCompletedEvent = new OrderEvent(this, testOrder, OrderEventTypeEnum.COMPLETED);

        // Настройка логгера для тестов (подробное описание есть в классе OrderFlowIntegrationTest)
        Logger logger = (Logger) LoggerFactory.getLogger(OrderEventListener.class);
        listAppender = new ListAppender<>();
        listAppender.start();

        // Очищаем список логов перед каждым тестом
        if (logger.getAppender("testAppender") != null) {
            logger.detachAppender("testAppender");
        }
        listAppender.setName("testAppender");
        logger.addAppender(listAppender);
        logger.setLevel(Level.INFO);
        listAppender.list.clear();
    }

    @Test
    void handleOrderCreatedEvent_ShouldProcessCreatedEvent() {
        // when
        orderEventListener.handleOrderCreatedEvent(orderCreatedEvent);

        // then
        List<ILoggingEvent> logsList = listAppender.list;

        // Проверяем специфичное сообщение для создания заказа
        assertTrue(logsList.stream()
                        .anyMatch(event -> event.getFormattedMessage().contains("Новый заказ создан!")),
                "Лог должен содержать сообщение о создании заказа");
    }

    @Test
    void handleOrderCompletedEvent_ShouldProcessCompletedEvent() {
        // when
        orderEventListener.handleOrderCompletedEvent(orderCompletedEvent);

        // then
        List<ILoggingEvent> logsList = listAppender.list;

        // Проверяем специфичное сообщение для завершения заказа
        assertTrue(logsList.stream()
                        .anyMatch(event -> event.getFormattedMessage().contains("Заказ выполнен!")),
                "Лог должен содержать сообщение о выполнении заказа");
    }

    @Test
    void handleOrderEvent_ShouldProcessAllEvents() {
        // when
        orderEventListener.handleOrderEvent(orderCreatedEvent);

        // then
        List<ILoggingEvent> logsList = listAppender.list;

        // Проверяем общие сообщения
        assertTrue(logsList.stream()
                        .anyMatch(event -> event.getFormattedMessage().contains("Получено событие заказа")),
                "Лог должен содержать сообщение о получении события");
        assertTrue(logsList.stream()
                        .anyMatch(event -> event.getFormattedMessage().contains("Заказ ID: " + testOrder.getId())),
                "Лог должен содержать ID заказа");
    }

    @Test
    void handleEvents_ShouldRespectOrderAnnotation() {
        // when
        orderEventListener.handleOrderCreatedEvent(orderCreatedEvent);
        orderEventListener.handleOrderEvent(orderCreatedEvent);

        // then
        List<ILoggingEvent> logsList = listAppender.list;

        // Проверяем порядок сообщений
        assertTrue(logsList.size() >= 2, "Должно быть как минимум два сообщения в логе");
        assertTrue(logsList.get(0).getFormattedMessage().contains("Новый заказ создан!"),
                "Первое сообщение должно быть о создании заказа");
        assertTrue(logsList.get(1).getFormattedMessage().contains("Получено событие заказа"),
                "Второе сообщение должно быть о получении события");
    }
}