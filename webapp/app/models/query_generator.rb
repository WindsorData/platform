class QueryGenerator

  def self.json_query(params_hash)
    query = {executives: [], advanced: []}
    params_hash.each_with_index { |(key, value), index|
      if key.starts_with? 'role'
        build_role_query(index, key, params_hash, query)
      else
        build_advanced_search_query(key, params_hash, query)
      end
    }
    
    # Clear values
    query.keys.each { |k|
      query.delete(k) if query[k].empty?
    }
    query[:executives][0][:executivesFilters].delete_if {|x| x.blank? }
    query[:executives][0].delete(:executivesFilters) if query[:executives][0][:executivesFilters].blank?
    query[:executives].delete_if {|x| x.blank?}
    query.delete(:executives) if query[:executives].blank?

    if query.blank?
      ""
    else
      query.to_json.gsub!(/\"/, '\'').gsub(/'(\d)\'/,' \1')
    end
  end

  def self.build_advanced_search_query(k1, params_hash, query)
    if params_hash[k1].is_a? Hash
      params_hash[k1].each { |k2, v2|
        if !params_hash[k1][k2]['gt'].blank? && !params_hash[k1][k2]['lt'].blank?
          hash = {key: k1 + "." + k2, operators: [{operator: 'gt', value: params_hash[k1][k2]['gt']}, {operator: 'lt', value: params_hash[k1][k2]['lt']}]}
        elsif !params_hash[k1][k2]['gt'].blank?
          hash = {key: k1 + "." + k2, operators: [{operator: 'gt', value: params_hash[k1][k2]['gt']}]}
        elsif !params_hash[k1][k2]['lt'].blank?
          hash = {key: k1 + "." + k2, operators: [{operator: 'lt', value: params_hash[k1][k2]['lt']}]}
        end
        query[:advanced] << hash
      }
    end
  end

  def self.build_role_query(i1, k1, params_hash, query)
    query[:executives][i1] = {executivesFilters: [([])]}
    params_hash[k1].each_with_index { |(k2, v2), i2|
      if params_hash[k1][k2].is_a? Hash
        if !params_hash[k1][k2]['type'].blank?
          if !params_hash[k1][k2]['gt'].blank? && !params_hash[k1][k2]['lt'].blank?
            hash = {key: k2 + "." + params_hash[k1][k2]['type'].downcase, operators: [{operator: 'gt', value: params_hash[k1][k2]['gt']},
                                                                                      {operator: 'lt', value: params_hash[k1][k2]['lt']}]}
          elsif !params_hash[k1][k2]['gt'].blank?
            hash = {key: k2 + "." + params_hash[k1][k2]['type'].downcase, operators: [{operator: 'gt', value: params_hash[k1][k2]['gt']}]}
          elsif !params_hash[k1][k2]['lt'].blank?
            hash = {key: k2 + "." + params_hash[k1][k2]['type'].downcase, operators: [{operator: 'lt', value: params_hash[k1][k2]['lt']}]}
          end
        else
          if !params_hash[k1][k2]['gt'].blank? && !params_hash[k1][k2]['lt'].blank?
            hash = {key: k2, operators: [{gt: params_hash[k1][k2]['gt'], lt: params_hash[k1][k2]['lt']}]}
          elsif !params_hash[k1][k2]['gt'].blank?
            hash = {key: k2, operators: [{gt: params_hash[k1][k2]['gt']}]}
          elsif !params_hash[k1][k2]['lt'].blank?
            hash = {key: k2, operators: [{lt: params_hash[k1][k2]['lt']}]}
          end
        end
      else
        hash = {key: k2, value: params_hash[k1][k2]} unless params_hash[k1][k2].blank?
      end
      query[:executives][i1][:executivesFilters][i2] = hash if hash
    }
  end
end