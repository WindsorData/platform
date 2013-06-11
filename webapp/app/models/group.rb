# == Schema Information
#
# Table name: groups
#
#  id         :integer          not null, primary key
#  created_at :datetime         not null
#  updated_at :datetime         not null
#  name       :string(255)
#  company_id :integer
#  user_id    :integer
#

class Group < ActiveRecord::Base
  attr_accessible :name, :tickers_tokens, :company
  attr_reader :tickers_tokens
  has_and_belongs_to_many :tickers
  belongs_to :company
  belongs_to :user

  validates :name, presence: true, uniqueness: { scope: :company_id}

  scope :by_company, lambda { |company| where(company_id: company.id) }

  def tickers_tokens=(ids)
    self.ticker_ids =  ids.split(",")
  end
end
