class BackendService
  HOST_URL = Rails.application.config.backend_host

  def self.post_file(type, file)
    case type
    when 'top5'
      path = Rails.application.config.post_top5_path
    when 'guidelines'
      path = Rails.application.config.post_guidelines_path
    when 'dilution'
      path = Rails.application.config.post_dilution_path
    when 'batch'
      path = Rails.application.config.post_batch_path
    end
    RestClient.post(HOST_URL + path, dataset: File.new(file.path, 'r'))
  end

  def self.perform_search(query)
    RestClient.post(HOST_URL + path, query: query)
  end

  def self.load_values
    load_tickers
    load_primary_roles
    load_secondary_roles
    load_cash_compensations
  end

  private
  def self.load_tickers
    json = RestClient.get(HOST_URL + Rails.application.config.get_tickers_path)
    Ticker.load_json(json)
  end

  def self.load_primary_roles
    json = RestClient.get(HOST_URL + Rails.application.config.get_primary_roles_path)
    PrimaryRole.load_json(json)
  end

  def self.load_secondary_roles
    json = RestClient.get(HOST_URL + Rails.application.config.get_secondary_roles_path)
    SecondaryRole.load_json(json)
  end

  def self.load_cash_compensations
    json = RestClient.get(HOST_URL + Rails.application.config.get_cash_compensations_path)
    CashCompensation.load_json(json)
  end
end