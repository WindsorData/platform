# Read about factories at https://github.com/thoughtbot/factory_girl

FactoryGirl.define do
  factory :user do
    password "123456"
    password_confirmation "123456"

    factory :super do
      email "super@windsor.com"
      role 'super'
    end
    
    factory :admin do
      email "admin@windsor.com"
      role 'admin'
    end
    
    factory :client do
      email "client@windsor.com"
      role 'client'
    end
  end
end
