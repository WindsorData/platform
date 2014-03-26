require 'active_support'

class UploadLogController < ApplicationController
  before_filter {authorize!(:audit, :upload_log) }

  def audit
    @users = User.order('email desc')
    @tickers = DetailUploadFile.select("distinct(ticker)").where("ticker is not null").order("ticker")
  end

  def search
    params_hash = params.except(:controller, :action, :authenticity_token, :utf8)
    user_id = params_hash['user'].to_i unless params_hash['user'].blank?
    since = params_hash['date_since'].try(:to_date).try(:beginning_of_day)
    to = params_hash['date_to'].try(:to_date).try(:end_of_day)

    ticker = params_hash['upload-log-ticker'] unless params_hash['upload-log-ticker'].blank?
    @logs = UploadLog
              .with_user(user_id)
              .with_ticker(ticker)
              .created_since(since)
              .created_to(to)
              .order('created_at desc').paginated(params)

    respond_to do |format|
      format.js { render 'display_search'}
    end
  end

  def upload_log_file
    @logs = UploadLog.find_all_by_id(params["logs_ids"].split(","))
    respond_to do |format|
      format.xls { render 'upload_log_file'}
    end
  end

  def to_PST_date(date)
    Time.parse(date.to_s)
      .in_time_zone("Pacific Time (US & Canada)")
      .strftime('%B %e at %l:%M %p')
  end
end
