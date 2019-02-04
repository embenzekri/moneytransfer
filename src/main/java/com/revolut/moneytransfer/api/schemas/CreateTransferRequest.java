package com.revolut.moneytransfer.api.schemas;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL) 
public class CreateTransferRequest   {
  
  private String requestId = null;
  private String fromAccountId = null;
  private String toAccountId = null;
  private BigDecimal amount = null;
  private String currency = null;

  public CreateTransferRequest () {

  }

  public CreateTransferRequest (String requestId, String fromAccountId, String toAccountId, BigDecimal amount, String currency) {
    this.requestId = requestId;
    this.fromAccountId = fromAccountId;
    this.toAccountId = toAccountId;
    this.amount = amount;
    this.currency = currency;
  }

    
  @JsonProperty("requestId")
  public String getRequestId() {
    return requestId;
  }
  public void setRequestId(String requestId) {
    this.requestId = requestId;
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateTransferRequest createTransferRequest = (CreateTransferRequest) o;
    return Objects.equals(requestId, createTransferRequest.requestId) &&
        Objects.equals(fromAccountId, createTransferRequest.fromAccountId) &&
        Objects.equals(toAccountId, createTransferRequest.toAccountId) &&
        Objects.equals(amount, createTransferRequest.amount) &&
        Objects.equals(currency, createTransferRequest.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestId, fromAccountId, toAccountId, amount, currency);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateTransferRequest {\n");
    
    sb.append("    requestId: ").append(toIndentedString(requestId)).append("\n");
    sb.append("    fromAccountId: ").append(toIndentedString(fromAccountId)).append("\n");
    sb.append("    toAccountId: ").append(toIndentedString(toAccountId)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
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
