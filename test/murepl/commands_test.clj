(ns murepl.commands-test
  (:require [murepl.commands :refer :all]
            [murepl.testutils :refer [test-db reset-db!]]
            [murepl.user :as u]
            [schema.test :refer [validate-schemas]]
            [clojure.test :refer :all]))

(use-fixtures :each (fn [f] (reset-db! test-db) (f)))
(use-fixtures :once validate-schemas)

(defmacro with-swap [called-atom fn-symbol & body]
  `(with-redefs [~fn-symbol (fn [& _#] (swap! ~called-atom (constantly true)))]
     (do ~@body)
     (swap! ~called-atom (constantly false))))

(def borges {:name "borges" :password "labyrinth" :data {}})

(deftest dispatch-test
  (testing "when dispatching"
    (let [called (atom false)]
      (testing "and the command is"
        (testing "new it is called"
          (with-swap called murepl.commands/new-user!
            (dispatch* nil test-db nil nil "/new pukey puke")
            (is @called)))
        (testing "login it is called"
          (with-swap called murepl.commands/login!
            (dispatch* nil test-db nil nil "/login puker puke")
            (is @called)))
        (testing "change-password it is called"
          (with-swap called murepl.commands/change-password!
            (dispatch* nil test-db borges nil "/change-password foobarbaz")
            (is @called)))
        (testing "set-info it is called"
          (with-swap called murepl.commands/set-info!
            (dispatch* nil test-db borges nil "/set foobar baz")
            (is @called)))
        (testing "help it is called"
          (with-swap called murepl.commands/help
            (dispatch* nil test-db nil nil "/help")
            (is @called)))))
    (testing "and the command is not found"
      (is (re-find #"oops.*/help" (dispatch* nil test-db nil nil "/oh my"))))))

(deftest new-user-test
  (testing "when creating a new user"

    (testing "and the user doesn't exist"
      (let [command-str "/new \"borges\" \"labyrinth\""
            result (dispatch* nil test-db nil nil command-str)
            db-result (u/lookup borges test-db)]
        (testing "get back message"
          (is (re-find #"created" result)))
        (testing "the user is created"
          (is (= "borges" (:name db-result)))
          (is (= "labyrinth" (:password db-result))))))

    (testing "but username already exists"
      (testing "an appropriate message is returned"
        (let [command-str "/new \"borges\" \"labyrinth\""
              result (dispatch* nil test-db nil nil command-str)]
          (is (re-find #"already exists" result)))))

    (testing "but the command is malformed"
      (testing "an appropriate message is returned"
        (let [command-str "/new borges labyrinth"
              result (dispatch* nil test-db nil nil command-str)]
          (is (re-find #"try again.*new" result)))))))

(deftest change-password-test
  (testing "when changing a user's password"
    (u/new! borges test-db)

    (testing "and command is not malformed"
      (let [command-str "/change-password \"aleph\""
            result (dispatch* nil test-db borges nil command-str)
            db-result (u/lookup borges test-db)]
        (testing "it is updated"
          (= "aleph" (:password db-result)))
        (testing "appropriate message returned"
          (is (re-find #"updated" result)))))

    (testing "but the command is malformed"
      (let [command-str "/change-password foobarbaz"
            result (dispatch* nil test-db borges nil command-str)]
        (testing "an appropriate message is returned"
          (is (re-find #"try again.*change-password" result)))))))

(deftest set-info-test
  (testing "when updating a user's info"
    (u/new! borges test-db)

    (testing "and the command is not malformed"
      (testing "and stuff is added"
        (let [command-str "/set-info \"favorite color\" \"yellow\""
              result (dispatch* nil test-db borges nil command-str)
              db-result (u/lookup borges test-db)]
          (testing "the user's info is updated"
            (is (= "yellow" (get-in db-result [:data "favorite color"]))))
          (testing "an approprate message is returned"
            (is (re-find #"updated.*favorite color.*yellow" result)))))

      (testing "and existing stuff is changed"
        (let [command-str "/set-info \"favorite color\" \"red\""
              result (dispatch* nil test-db borges nil command-str)
              db-result (u/lookup borges test-db)]
          (testing "the user's info is updated"
            (is (= "red" (get-in db-result [:data "favorite color"]))))
          (testing "an approprate message is returned"
            (is (re-find #"updated.*favorite color.*red" result)))))

      (testing "and stuff is removed"
        (let [command-str "/set-info \"favorite color\" \"\""
              result (dispatch* nil test-db borges nil command-str)
              db-result (u/lookup borges test-db)]
          (testing "the user's info is updated"
            (is (nil? (get-in db-result [:data "favorite color"]))))
          (testing "an appropriate message is returned"
            (is (re-find #"removed.*favorite color" result))))))

    (testing "and the command is malformed"
      (let [command-str "/set-info foo bar"
            result (dispatch* nil test-db borges nil command-str)]
        (testing "an appropriate message is returned"
          (is (re-find #"try again.*set-info" result)))))))

(deftest login-test
  (testing "when logging in"
    (testing "and the command is not malformed"
      (testing "but the un/pw is incorrect"
        (testing "and approprirate message is returned"))
      (testing "and the user exists"
        (testing "channel is associated with user")
        (testing "appropriate message is returned")))
    (testing "and the command is malformed"
      (testing "an appropriate message is returned"))))

(deftest logout-test)
(deftest help-test)