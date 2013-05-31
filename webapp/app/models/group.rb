# == Schema Information
#
# Table name: groups
#
#  id         :integer          not null, primary key
#  created_at :datetime         not null
#  updated_at :datetime         not null
#  name       :string(255)
#

class Group < ActiveRecord::Base
  attr_accessible :name, :tickers_tokens
  has_and_belongs_to_many :tickers
  attr_reader :tickers_tokens

  def tickers_tokens=(ids)
    self.ticker_ids =  ids.split(",")
  end
end