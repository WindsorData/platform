require 'active_support'

class UploadLogController < ApplicationController
  before_filter {authorize!(:audit, :upload_log) }

  def audit
    params_hash = params.except(:controller, :action, :authenticity_token, :utf8)
    user_id = params_hash['user'].to_i unless params_hash['user'].blank?
    since = params_hash['date_since'].try(:to_date).try(:beginning_of_day)
    to = params_hash['date_to'].try(:to_date).try(:end_of_day)
    ticker = params_hash['ticker_name'] unless params_hash['ticker_name'].blank?

    @logs = UploadLog
              .with_user(user_id)
              .with_ticker(ticker)
              .created_since(since)
              .created_to(to)
              .order('created_at desc').paginated(params)
    @users = User.order('email desc')
    @tickers = DetailUploadFile.select("distinct(ticker)").where("ticker is not null").order("ticker")
  end
end
