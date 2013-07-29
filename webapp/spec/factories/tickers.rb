FactoryGirl.define do
  factory :ticker do
    sequence(:name) {|n| "name#{n}" }
    sequence(:ticker) {|n| "ticker#{n}" }
    sequence(:cusip) {|n| "cusip#{n}" }
  end
end