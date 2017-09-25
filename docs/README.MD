## Description of the *simple_message_broker's* protocol

The protocol for the system is based on JSON.
The protocol exposes the following commands:

- `send` - send a message to the queue;
- `receive` - read a message from the queue;
- `close` - disconnect from the server;
- `createQueue` - create a new persistent queue;
- `subscribe` - subscribe to a queue;
- `batchSubscribe` - subscribe to multiple queues;
- `patternBatchSubscribe` - subscribe to multiple queues using a regEx;
- `multicast` - send a message to multiple queues using a regEx;
- `durableSend` - send a persistent message to the queue;
- `acknowledgeReceive` - acknowledge that a message was received;

However, it also has other types of messages:

- `subscriptionMessage` - an incoming message (pushed to subscribers);
- `error` - an error message (response sent to the client);
- `response` - an operation status message (response sent to the client);

Example of the structure for a protocol message:

```json
{
    "command": "<send|receive|close|createQueue|subscribe|batchSubscribe|patternBatchSubscribe|multicast|durableSend|acknowledgeReceive|error>",
    "targetQueueName": "<queueName>",
    "payload": "<payload>"
}
```
For example, to create a queue, the client would send the following message:
```json
{
    "command": "createQueue",
	"targetQueueName": "AAPL.Q",
    "payload": ""
}
```
And in that case the server will respond with the following:
```json
{
    "command": "response",
	"targetQueueName": "",
    "payload": "success"
}
```
And in case of an error, the server will response with a response like this:
```json
{
    "command": "error",
    "payload": "<payload>"
}
```
The must contain the following fields:
- `payload` -  is payload of sent/received message. For an `error` message, the `payload`
contains some info about the error.
- `command` -  is type of action to be executed by the broker for the `command` message.
- `targetQueueName` - the name of the queue on which the command operates (if that makes sense).

Here's a list of example of possible messages:

- `send` - send a message to the queue;
    ```json
    {
        "command": "send",
    	"targetQueueName": "AAPL.Q",
        "payload": "This is the message's payload."
    }
    ```
- `receive` - read a message from the queue;
    ```json
    {
        "command": "receive",
    	"targetQueueName": "AAPL.Q",
        "payload": ""
    }
    ```
- `close` - disconnect from the server;
    ```json
    {
        "command": "close",
    	"targetQueueName": "",
        "payload": ""
    }
    ```
- `createQueue` - create a new persistent queue;
    ```json
    {
        "command": "createQueue",
    	"targetQueueName": "AAPL.Q",
        "payload": ""
    }
    ```
- `subscribe` - subscribe to a queue;
    ```json
    {
        "command": "subscribe",
    	"targetQueueName": "AAPL.Q",
        "payload": ""
    }
    ```
- `batchSubscribe` - subscribe to multiple queues;
    ```json
    {
        "command": "batchSubscribe",
    	"targetQueueName": "AAPL.Q, Amazon.Q",
        "payload": ""
    }
    ```
- `patternBatchSubscribe` - subscribe to multiple queues using a regEx;
    ```json
    {
        "command": "patternBatchSubscribe",
    	"targetQueueName": "A.+",
        "payload": ""
    }
    ```
- `multicast` - send a message to multiple queues using a regEx;
    ```json
    {
        "command": "multicast",
    	"targetQueueName": "A.+",
        "payload": "This is the message"
    }
    ```
- `durableSend` - send a persistent message to the queue;
    ```json
    {
        "command": "durableSend",
    	"targetQueueName": "AAPL.Q",
        "payload": "The payload"
    }
    ```
- `acknowledgeReceive` - acknowledge that a message was received (the payload is the *messageId*);
    ```json
    {
        "command": "acknowledgeReceive",
    	"targetQueueName": "",
        "payload": "1234"
    }
    ```
