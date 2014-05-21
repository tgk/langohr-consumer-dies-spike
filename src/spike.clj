(ns spike
  (:require [langohr.core          :as rmq]
            [langohr.channel       :as lch]
            [langohr.queue         :as lq]
            [langohr.consumers     :as lc]
            [langohr.basic         :as lb])
  (:import com.novemberain.langohr.Channel))

(def exchange "")

(defn ensure-queue
  [ch queue]
  (lq/declare ch queue :durable true :auto-delete false))

(defn- remove-queue
  [queue]
  (let [conn     (rmq/connect)
        ch       (lch/open conn)]
    (lq/delete ch queue)
    (rmq/close ch)
    (rmq/close conn)))

(defn enqueue
  [queue]
  (let [conn     (rmq/connect)
        ch       (lch/open conn)]
    (try (ensure-queue ch queue)
         (println "Putting messages on queue")
         (doseq [i (range 25)]
           (lb/publish ch exchange queue
                       (str "Hello " i)
                       :content-type "text/plain"))
         (finally (rmq/close ch)
                  (rmq/close conn)))))

(defn rabbitmq-message-handler
  [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
  (let [s (String. payload)]
    (Thread/sleep 5)
    (print "Received" s)
    (if (< 0.9 (rand))
      (do
        (println " Throwing exception")
        (throw (ex-info "Dummy exception" {:string s})))
      (println " Doing nothin'"))))

(defn our-ack-unless-exception
  [f rethrow?]
  (fn [^Channel channel {:keys [delivery-tag] :as metadata} body]
    (try
      (f channel metadata body)
      (.basicAck channel delivery-tag false)
      (catch Exception e
        (.basicReject channel delivery-tag true)
        (println "Rejected")
        (when rethrow?
          (throw e))))))

(defn listen
  [queue rethrow?]
  (println "Creating listener")
  (let [conn (rmq/connect)
        ch   (doto (lch/open conn)
               (lb/qos 1))]
    (ensure-queue ch queue)
    (lc/subscribe ch queue
                  (our-ack-unless-exception
                   rabbitmq-message-handler
                   rethrow?)
                  :auto-ack false)
    (fn []
      (try (rmq/close ch)
           (catch Exception e))
      (try (rmq/close conn)
           (catch Exception e)))))

(defn perform-experiment
  [queue rethrow?]
  (println)
  (println "-------------------")
  (println queue)
  (println)
  (remove-queue queue)
  (Thread/sleep 1000)
  (let [stop-listen (listen queue rethrow?)]
    (enqueue queue)
    (Thread/sleep 5000)
    (stop-listen)
    (println "Stopped consumer")))

(perform-experiment "without-re-throw" false)

(perform-experiment "re-throw" true)
