package org.example.listeners.service;

import org.example.listeners.enums.OrderEventTypeEnum;
import org.example.listeners.enums.OrderStatusEnum;
import org.example.listeners.event.OrderEvent;
import org.example.listeners.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    /*
    @Captor - это аннотация из библиотеки Mockito, которая используется для
    автоматического создания экземпляра ArgumentCaptor.
    Важно отметить, что @Captor работает только вместе с:
      Аннотацией @ExtendWith(MockitoExtension.class) на уровне класса (как в нашем случае)
      Или с @RunWith(MockitoJUnitRunner.class) для более старых версий JUnit
    Это часть более широкой системы аннотаций Mockito, куда входят:
      @Mock - для создания моков
      @Spy - для создания шпионов
      @InjectMocks - для внедрения моков
      @Captor - для создания капторов аргументов

    ArgumentCaptor из библиотеки Mockito используется для "захвата" аргументов, которые передаются
    в мокированные методы. Давайте разберем подробнее, как это работает в контексте OrderServiceTest:
      Давайте разберем, как работает ArgumentCaptor в этом тесте:
        Сначала у нас есть мок издателя событий:
          @Mock
          private ApplicationEventPublisher eventPublisher;
        Затем определен ArgumentCaptor:
          @Captor
          private ArgumentCaptor<OrderEvent> orderEventCaptor;
        В тестах он используется так:
          // Вызываем метод, который должен опубликовать событие
          orderService.createOrder(testOrder);

          // Проверяем что метод publishEvent был вызван и захватываем аргумент
          verify(eventPublisher).publishEvent(orderEventCaptor.capture());

          // Получаем захваченное событие
          OrderEvent capturedEvent = orderEventCaptor.getValue();

          // Проверяем его свойства
          assertEquals(OrderEventTypeEnum.CREATED, capturedEvent.getEventType());
      Это похоже на "видеозапись" того, какой аргумент был передан в метод. Представьте это так:
        Без ArgumentCaptor:
          // Можем только проверить, что метод был вызван
          verify(eventPublisher).publishEvent(any());
          // Но не можем проверить, с каким именно аргументом
        С ArgumentCaptor:
          // Можем "записать" аргумент
          verify(eventPublisher).publishEvent(orderEventCaptor.capture());
          // И потом проверить все его свойства
          OrderEvent event = orderEventCaptor.getValue();
          assertEquals(CREATED, event.getEventType());
          assertEquals(testOrder, event.getOrder());
      Это особенно полезно когда:
        Нужно проверить сложные объекты, переданные в мокированный метод
        Объект создается внутри тестируемого метода и мы не имеем к нему прямого доступа
        Нужно проверить несколько свойств переданного аргумента
      В данном случае ArgumentCaptor используется для проверки, что:
        Событие было создано с правильным типом (CREATED или COMPLETED)
        В событие был передан правильный заказ
        Статус заказа был обновлен правильно
      Это делает тесты более надежными, так как мы проверяем не только факт вызова метода,
      но и корректность всех данных, которые в него передаются.
    */
    @Captor
    private ArgumentCaptor<OrderEvent> orderEventCaptor;

    private OrderService orderService;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(eventPublisher);
        testOrder = new Order(1L, OrderStatusEnum.NEW, 100.0);
    }

    @Test
    void createOrder_ShouldPublishCreatedEvent() {
        // when
        orderService.createOrder(testOrder);

        // then
        verify(eventPublisher).publishEvent(orderEventCaptor.capture());
        OrderEvent capturedEvent = orderEventCaptor.getValue();

        assertEquals(OrderEventTypeEnum.CREATED, capturedEvent.getEventType());
        assertEquals(OrderStatusEnum.CREATED, testOrder.getStatus());
        assertEquals(testOrder, capturedEvent.getOrder());
    }

    @Test
    void completeOrder_ShouldPublishCompletedEvent() {
        // when
        orderService.completeOrder(testOrder);

        // then
        verify(eventPublisher).publishEvent(orderEventCaptor.capture());
        OrderEvent capturedEvent = orderEventCaptor.getValue();

        assertEquals(OrderEventTypeEnum.COMPLETED, capturedEvent.getEventType());
        assertEquals(OrderStatusEnum.COMPLETED, testOrder.getStatus());
        assertEquals(testOrder, capturedEvent.getOrder());
    }
}