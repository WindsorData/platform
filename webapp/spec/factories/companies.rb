FactoryGirl.define do
  factory :company do
    sequence(:name) {|n| "Company #{n}" }
    address "Fake St 123"
    contact_email "contact@company.com"
  end
end