package com.revolut.moneytransfer.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL) 
public class Transfer   {
  
  private String id = null;
  private String fromAccountId = null;
  private String toAccountId = null;
  private BigDecimal amount = null;
  private String currency = null;
  private String state = null;

  public Transfer () {

  }

  public Transfer (String id, String fromAccountId, String toAccountId, BigDecimal amount, String currency, String state) {
    this.id = id;
    this.fromAccountId = fromAccountId;
    this.toAccountId = toAccountId;
    this.amount = amount;
    this.currency = currency;
    this.state = state;
  }

    
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

    
  @JsonProperty("fromAccountId")
  public String getFromAccountId() {
    return fromAccountId;
  }
  public void setFromAccountId(String fromAccountId) {
    this.fromAccountId = fromAccountId;
  }

    
  @JsonProperty("toAccountId")
  public String getToAccountId() {
    return toAccountId;
  }
  public void setToAccountId(String toAccountId) {
    this.toAccountId = toAccountId;
  }

    
  @JsonProperty("amount")
  public BigDecimal getAmount() {
    return amount;
  }
  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

    
  @JsonProperty("currency")
  public String getCurrency() {
    return currency;
  }
  public void setCurrency(String currency) {
    this.currency = currency;
  }

    
  @JsonProperty("state")
  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Transfer transfer = (Transfer) o;
    return Objects.equals(id, transfer.id) &&
        Objects.equals(fromAccountId, transfer.fromAccountId) &&
        Objects.equals(toAccountId, transfer.toAccountId) &&
        Objects.equals(amount, transfer.amount) &&
        Objects.equals(currency, transfer.currency) &&
        Objects.equals(state, transfer.state);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, fromAccountId, toAccountId, amount, currency, state);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Transfer {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    fromAccountId: ").append(toIndentedString(fromAccountId)).append("\n");
    sb.append("    toAccountId: ").append(toIndentedString(toAccountId)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
