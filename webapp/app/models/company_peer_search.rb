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

class CompanyPeerSearch < Search
  attr_accessible :group_name, :tickers
  validates :tickers, presence: true
  validates :group_name, presence: true

  before_create :set_report_type

  def set_report_type
    self.report_type = self.report_type + " - " + Constants::COMPANY_PEERS_SEARCH
  end
end
