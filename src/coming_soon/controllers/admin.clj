(ns coming-soon.controllers.admin
  (:require [compojure.core :refer (defroutes GET POST)]
            [clj-json.core :as json]
            [clojure.data.xml :as xml]
            [clojure-csv.core :as csv]
            [coming-soon.models.contact :refer (all-contacts)]
            [coming-soon.config :refer (admin-user admin-password)]
            [coming-soon.views.admin :as view]))
            
(defn- headers [mime-type extension]
  {
    "Cache-Control" "must-revalidate"
    "Pragma" "must-revalidate"
    "Content-Type" (str mime-type ";charset=utf-8")
    "Content-Disposition" (str "attachment;filename=contacts." extension)
  })

(defn authenticated? [username password]
  (and 
    (= username admin-user)
    (= password admin-password)))

(defn- json-contacts []
  {:status 200
   :headers (headers "application/json" "json")
   :body (json/generate-string (all-contacts))})

(defn- xml-contact 
  "Return the specified contact as an XML element"
  [contact]
  (xml/element :contact {:id (str (:id contact))} [
    (xml/element :email {} (:email contact))
    (xml/element :referrer {} (:referrer contact))
    (xml/element :updated-at {} (:updated-at contact))]))

(defn- xml-contacts []
  {:status 200
   :headers (headers "application/xml" "xml")
   :body (xml/emit-str (xml/element :contacts {} 
      (map xml-contact (all-contacts))))})

(def csv-header ["id" "email" "referrer" "updated-at"])

(defn- csv-contact 
  "Return the specified contact as a vector for CSV encoding"
  [contact]
  (list (str (:id contact)) (:email contact) (:referrer contact) (:updated-at contact)))

(defn- csv-contacts []
  {:status 200
   :headers (headers "text/csv" "csv")
   :body (csv/write-csv (cons csv-header (map csv-contact (all-contacts))))})

(defn- html-contacts []
  (apply str (view/admin-page (all-contacts))))

(defroutes admin-routes
  (GET "/contacts" [] (html-contacts))
  (GET "/contacts.html" [] (html-contacts))
  (GET "/contacts.json" [] (json-contacts))
  (GET "/contacts.xml" [] (xml-contacts))
  (GET "/contacts.csv" [] (csv-contacts)))