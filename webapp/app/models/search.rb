# == Schema Information
#
# Table name: searches
#
#  id          :integer          not null, primary key
#  user_id     :integer
#  json_query  :text
#  created_at  :datetime         not null
#  updated_at  :datetime         not null
#  company_id  :integer
#  report_type :string(255)
#  peers       :text
#  tickers     :text
#  type        :string(255)
#  group_name  :string(255)
#

class Search < ActiveRecord::Base
  attr_accessible :user, :company, :report_type
  belongs_to :user
  belongs_to :company

  validates :user_id, presence: true

  scope :by_company, lambda { |company, n|
    where(company_id: company.id).order("created_at desc").limit(n)
  }
  scope :last_ordered_by_date, lambda { |n|
    order("created_at desc").limit(n)
  }
end
