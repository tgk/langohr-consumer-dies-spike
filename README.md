# langohr-consumer-dies-spike

A spike demonstrating exceptions result in a process not receiving
subsequent messages.

I suspect it's my understanding of what `ack-unless-exception` is
supposed to do. I assumed the message would be put back in RabbitMQ, and
that we would be handled a new message, that could potentially be the
same.

## Usage

Start a repl using `lein` and load the `spike.clj` file. The error
manifests itself as no messages coming in after an exception has been
thrown.

```
$ lein repl
nREPL server started on port 63667 on host 127.0.0.1
REPL-y 0.2.1
Clojure 1.6.0
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)
 Results: Stored in vars *1, *2, *3, an exception in *e

user=> (load-file "src/spike.clj")
Creating listener
Putting messages on queue
Received succeed
Received succeed
Received succeed
Received succeed
Received succeed
Received succeed
Received succeed
Received succeed
Received succeed
Received succeed
Received succeed
Received fail
Throwing exception
DefaultExceptionHandler: Consumer langohr.consumers.proxy$com.rabbitmq.client.DefaultConsumer$ff19274a@7074c91e (amq.ctag-xmMhXlC6fvM0Izmehml_Fw) method handleDelivery for channel AMQChannel(amqp://guest@127.0.0.1:5672/,1) threw an exception for channel AMQChannel(amqp://guest@127.0.0.1:5672/,1):
clojure.lang.ExceptionInfo: Dummy exception {:string "fail"}
	at clojure.core$ex_info.invoke(core.clj:4403)
	at spike$rabbitmq_message_handler.invoke(spike.clj:33)
	at langohr.consumers$ack_unless_exception$fn__1565.invoke(consumers.clj:124)
	at langohr.consumers$create_default$fn__1526.invoke(consumers.clj:61)
	at langohr.consumers.proxy$com.rabbitmq.client.DefaultConsumer$ff19274a.handleDelivery(Unknown Source)
	at com.rabbitmq.client.impl.ConsumerDispatcher$5.run(ConsumerDispatcher.java:140)
	at com.rabbitmq.client.impl.ConsumerWorkService$WorkPoolRunnable.run(ConsumerWorkService.java:85)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at java.lang.Thread.run(Thread.java:744)

CompilerException com.rabbitmq.client.AlreadyClosedException: channel is already closed due to clean channel shutdown; protocol method: #method<channel.close>(reply-code=200, reply-text=Closed due to exception from Consumer langohr.consumers.proxy$com.rabbitmq.client.DefaultConsumer$ff19274a@7074c91e (amq.ctag-xmMhXlC6fvM0Izmehml_Fw) method handleDelivery for channel AMQChannel(amqp://guest@127.0.0.1:5672/,1), class-id=0, method-id=0), compiling:(/Users/thomas/dev/langohr-consumer-dies-spike/src/spike.clj:47:26)
user=>
```

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
