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

class Top5Search < Search
  attr_accessible :json_query
  validates :json_query, presence: true

  before_create :set_report_type

  def set_report_type
    self.report_type = Constants::INCOMING_PEERS_SEARCH
  end
end
