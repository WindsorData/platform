FactoryGirl.define do
  factory :user do
    password "123456"
    password_confirmation "123456"

    factory :super do
      sequence(:email) {|n| "super#{n}@windsor.com" }
      role 'super'
    end
    
    factory :admin do
      sequence(:email) {|n| "admin#{n}@windsor.com" }
      role 'admin'
    end
    
    factory :client do
      sequence(:email) {|n| "client#{n}@windsor.com" }
      role 'client'
      company
    end
  end
end
