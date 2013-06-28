FactoryGirl.define do
  factory :ticker do
    sequence(:name) {|n| "ticker#{n}" }
  end
end