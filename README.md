
#Задание №3
-----------------------------------

1. Изменена сущность Account:
- добавить статус [ARRESTED, BLOCKED, CLOSED, OPEN]
- добавить уникальный accountId
- добавить поле frozenAmount;

Изменена сущность Client 
- добавить уникальный clientId

2. Оганизован блок Operate и доработан сервис при получении сообщения из топика t1_demo_transactions:

- проверяет статус счета: если статус OPEN, то:
- сохраняет транзакцию в БД со статусом REQUESTED
- изменяет счет клиента на сумму транзакции, отправляет сообщение в топик t1_demo_transaction_accept с информацией {clientId, accountId, transactionId, timestamp, transaction.amount, account.balance}

Доработан сервис :

- Сервис слушает топик t1_demo_transaction_accept
При получении сообщения:
- Если транзакции по одному и тому же клиенту и счету приходят больше N раз в Т времени (настраивается в конфиге) и timestamp транзакции попадает в этот период, то N транзакциям присвоить статус BLOCKED, сообщение со статусом, id счета и id транзакции отправить в топик t1_demo_transaction_result
- Если сумма списания в транзакции больше, чем баланс счета - отправить сообщение со статусом REJECTED
- Если всё ок, то статус ACCECPTED

Доработан сервис :
теперь слушает еще и топик t1_demo_transaction_result:

- При получении сообщения со статусом ACCECPTED - обновляет статус транзакции в БД
- При получении BLOCKED - обновляет транзакциям статусы в БД и выставляет счёту статус BLOCKED. Баланс счёта меняется следующим образом: счет корректируется на сумму заблокированных транзакций, сумма записывается в поле frozenAmount



#Задание №2
-----------------------------------


1. Изменен аспект @LogDataSourceError- теперь аспект может отсылать сообщение в топик t1_demo_metrics.
   В заголовке - тип ошибки: DATA_SOURCE;
   В случае, если отправка не удалась - пишет в БД.


2. Разработан аспект @Metric, принимающий в качестве значения время в миллисекундах.
   Если время работы метода превышает задаое значение, аспект отправляет сообщение в топик Kafka (t1_demo_metrics) c информацией о времени работы, имени метода и параметрах метода, если таковые имеются. В заголовке - тип ошибки METRICS.


3. Реализованы 2 консьюмера (KafkaAccountConsumer, KafkaTransactionConsumer), слушающих топики t1_demo_accounts и t1_demo_transactions. При получении сообщения сервис сохраняет транзакцию в БД.

Для проверки консьюмеров реализованы продюсеры аккаунтов и транзакций (KafkaAccountProducer, KafkaTransactionProducer).
Так же добавлены тестовые методы SendToKafka - для подстановки и проверки значений.

