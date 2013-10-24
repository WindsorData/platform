# encoding: UTF-8
# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended to check this file into your version control system.

ActiveRecord::Schema.define(:version => 20131024193230) do

  create_table "companies", :force => true do |t|
    t.string   "name"
    t.string   "address"
    t.string   "contact_email"
    t.datetime "created_at",    :null => false
    t.datetime "updated_at",    :null => false
  end

  create_table "company_peers", :force => true do |t|
    t.string   "ticker",     :null => false
    t.string   "name"
    t.datetime "created_at", :null => false
    t.datetime "updated_at", :null => false
  end

  create_table "compensations", :force => true do |t|
    t.string   "field"
    t.string   "value"
    t.datetime "created_at", :null => false
    t.datetime "updated_at", :null => false
    t.string   "type"
  end

  create_table "detail_upload_files", :force => true do |t|
    t.text    "file_name"
    t.integer "upload_log_id"
    t.text    "messages"
    t.string  "ticker"
  end

  add_index "detail_upload_files", ["ticker"], :name => "index_detail_upload_files_on_ticker"
  add_index "detail_upload_files", ["upload_log_id"], :name => "index_detail_upload_files_on_upload_log_id"

  create_table "groups", :force => true do |t|
    t.datetime "created_at", :null => false
    t.datetime "updated_at", :null => false
    t.string   "name"
    t.integer  "company_id"
    t.integer  "user_id"
  end

  create_table "groups_tickers", :id => false, :force => true do |t|
    t.integer "group_id"
    t.integer "ticker_id"
  end

  add_index "groups_tickers", ["group_id", "ticker_id"], :name => "index_groups_tickers_on_group_id_and_ticker_id"
  add_index "groups_tickers", ["ticker_id", "group_id"], :name => "index_groups_tickers_on_ticker_id_and_group_id"

  create_table "roles", :force => true do |t|
    t.string   "name"
    t.datetime "created_at", :null => false
    t.datetime "updated_at", :null => false
    t.string   "type"
  end

  create_table "searches", :force => true do |t|
    t.integer  "user_id"
    t.text     "json_query"
    t.datetime "created_at",  :null => false
    t.datetime "updated_at",  :null => false
    t.integer  "company_id"
    t.string   "report_type"
    t.string   "peers"
    t.string   "tickers"
    t.string   "type"
  end

  create_table "tickers", :force => true do |t|
    t.string   "name"
    t.datetime "created_at", :null => false
    t.datetime "updated_at", :null => false
    t.string   "ticker"
    t.string   "cusip"
  end

  create_table "upload_logs", :force => true do |t|
    t.integer  "user_id"
    t.string   "upload_type"
    t.datetime "created_at",  :null => false
    t.datetime "updated_at",  :null => false
  end

  create_table "users", :force => true do |t|
    t.string   "email",                  :default => "", :null => false
    t.string   "encrypted_password",     :default => "", :null => false
    t.string   "reset_password_token"
    t.datetime "reset_password_sent_at"
    t.datetime "remember_created_at"
    t.integer  "sign_in_count",          :default => 0
    t.datetime "current_sign_in_at"
    t.datetime "last_sign_in_at"
    t.string   "current_sign_in_ip"
    t.string   "last_sign_in_ip"
    t.datetime "created_at",                             :null => false
    t.datetime "updated_at",                             :null => false
    t.string   "role"
    t.integer  "company_id"
  end

  add_index "users", ["email"], :name => "index_users_on_email", :unique => true
  add_index "users", ["reset_password_token"], :name => "index_users_on_reset_password_token", :unique => true

end
