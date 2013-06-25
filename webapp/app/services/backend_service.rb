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

  def self.update_search_values
    load_values(HOST_URL + Rails.application.config.get_tickers_path, Ticker)
    load_values(HOST_URL + Rails.application.config.get_primary_roles_path, PrimaryRole)
    load_values(HOST_URL + Rails.application.config.get_secondary_roles_path, SecondaryRole)
    load_values(HOST_URL + Rails.application.config.get_level_roles_path, LevelRole)
    load_values(HOST_URL + Rails.application.config.get_scope_roles_path, ScopeRole)
    load_values(HOST_URL + Rails.application.config.get_bod_roles_path, BodRole)
    load_values(HOST_URL + Rails.application.config.get_cash_compensations_path, CashCompensation)
    load_values(HOST_URL + Rails.application.config.get_equity_compensations_path, EquityCompensation)
  end

  private
  def self.load_values(path, clazz)
    json = RestClient.get(path)
    clazz.load_json(json)
  end
end