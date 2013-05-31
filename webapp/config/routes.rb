WindosorFrontend::Application.routes.draw do

  resources :companies

  root to: "home#index"

  devise_for :users
  resources :groups, only: [:new, :create] do
    collection do
      get :tickers
    end
  end

  get "dashboard/index"
  get "dashboard/search"
  post "search/results"
  resources :users, only: [:new, :create, :index, :destroy]

end
