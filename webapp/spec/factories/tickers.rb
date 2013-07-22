FactoryGirl.define do
  factory :ticker do
    sequence(:name) {|n| "ticker#{n}" }
    sequence(:ticker) {|n| "tkr#{n}" }
    sequence(:cusip) {|n| "cusip#{n}" }
  end
end