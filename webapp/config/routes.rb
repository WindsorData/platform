WindosorFrontend::Application.routes.draw do

  devise_scope :user do
    root to: "devise/sessions#new"
  end
  devise_for :users

  resources :users, except: [:show, :new] do
    collection do
      get 'edit_account'
      put 'update_account'
    end
  end

  resources :groups, except: [:new] do
    collection do
      get :tickers
    end
  end

  resources :companies do
    collection do
      get   :delete_info
      post  :delete_top5
      post  :delete_bod
      post  :delete_db
      post  :delete_peers
    end
  end

  # Search
  get "search/quick_search",  as: :quick_search
  get "search/filter_recent_search", as: :filter_recent_search
  get "search/full_search",   as: :full_search
  get "search/recent_search/:id", to: "search#recent_search", as: :top_5_recent_search
  get "search/search_log/:id", to: "search#search_log", as: :search_log


  post "search/results",      as: :search_result
  post "search/download",     as: :file_download
  post "search/group_search", as: :group_search

  # Files Upload
  get   "files/upload",       as: :file_upload
  post  "files/send_file",    as: :send_file

  # Company Peers
  get 'company_peers', to: 'company_peers#company_peers'
  get 'incoming_peers',        to: 'company_peers#incoming_peers'
  get 'peers_peers',        to: 'company_peers#peers_peers'
  post 'company_peers/incoming_peers_result', as: :incoming_peers_result
  post 'company_peers/peers_peers_single_ticker_result', as: :peers_peers_single_ticker_result
  post 'company_peers/peers_peers_ticker_list_result', as: :peers_peers_ticker_list_result

  # Export Files
  post 'companies/peers_inventory_file',     as: :export_peers_inventory
  post 'companies/companies_inventory_file', as: :export_companies_inventory

  post 'company_peers/peers_peers_single_ticker_file', as: :export_peers_peers_single_ticker
  post 'company_peers/peers_peers_ticker_list_file', as: :export_peers_peers_ticker_list
  post 'company_peers/peers_peers_raw_data_file', as: :export_peers_peers_raw_data
  post 'company_peers/peers_peers_raw_data_file_from_primary', as: :export_peers_peers_raw_data_from_primary

  post 'company_peers/incoming_peers_raw_data_file', as: :export_incoming_peers_raw_data

  # Upload Log
  get "upload_log/audit", as: :audit_log
  get "upload_log/search", as: :search_upload_log
  get "upload_log/upload_log_file", as: :export_upload_log

end

