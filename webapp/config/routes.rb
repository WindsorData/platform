WindosorFrontend::Application.routes.draw do
  root to: "home#index"
  get "dashboard/index"

  ActiveAdmin.routes(self)

  devise_for :admin_users, ActiveAdmin::Devise.config

  devise_for :users
  resources :groups, only: [:new, :index, :create]
  
end
