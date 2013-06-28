class BackendService

  def self.update_search_values
    path = Rails.application.config.backend_host
    load_values(path + Rails.application.config.get_tickers_path, Ticker)

    load_values(path + Rails.application.config.get_primary_roles_path, PrimaryRole)
    load_values(path + Rails.application.config.get_secondary_roles_path, SecondaryRole)
    load_values(path + Rails.application.config.get_level_roles_path, LevelRole)
    load_values(path + Rails.application.config.get_scope_roles_path, ScopeRole)
    load_values(path + Rails.application.config.get_bod_roles_path, BodRole)

    load_values(path + Rails.application.config.get_cash_compensations_path, CashCompensation)
    load_values(path + Rails.application.config.get_equity_compensations_path, EquityCompensation)
  end

  private
  def self.load_values(path, clazz)
    json = RestClient.get(path)
    clazz.load_json(json)
  end
end