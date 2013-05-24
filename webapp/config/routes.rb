WindosorFrontend::Application.routes.draw do

  root to: "home#index"

  ActiveAdmin.routes(self)
  devise_for :admin_users, ActiveAdmin::Devise.config

  devise_for :users
  resources :groups, only: [:new, :create] do
    collection do
      get :tickers
    end
  end

  get "dashboard/index"
  post "search/results"

end
