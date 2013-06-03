WindosorFrontend::Application.routes.draw do
  root to: "home#index"
  devise_for :users

  resources :users, except: [:show]
  resources :companies
  resources :groups, only: [:new, :create] do
    collection do
      get :tickers
    end
  end

  get "search/quick_search", as: :quick_search
  get "search/full_search", as: :full_search
  post "search/results"

end
