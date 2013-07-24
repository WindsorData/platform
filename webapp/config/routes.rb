WindosorFrontend::Application.routes.draw do

  root to: "home#index"
  devise_for :users

  resources :users, except: [:show, :new]
  resources :companies
  resources :groups, except: [:new] do
    collection do
      get :tickers
    end
  end

  get "search/quick_search",  as: :quick_search
  get "search/full_search",   as: :full_search
  get   "files/upload",       as: :file_upload
  post  "files/send_file",    as: :send_file
  get "search/recent_search/:id", to: "search#recent_search", as: :recent_search

  post "search/results",      as: :search_result
  post  "files/send_file",    as: :send_file
  post "search/download",     as: :file_download
  post "search/group_search", as: :group_search

  get "upload_log/audit",     as: :audit_log

  get 'company_peers/index',  as: :company_peers
  get 'company_peers',        to: 'company_peers#company_peers'
  
end
