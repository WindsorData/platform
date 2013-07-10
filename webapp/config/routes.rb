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
  post "search/results",      as: :search_result

  get   "files/upload",       as: :file_upload
  post  "files/send_file",    as: :send_file

end
