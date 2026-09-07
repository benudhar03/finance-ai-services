package com.finance.ai.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FinanceCalculatorTools {

    @Tool(description = "Calculates the monthly EMI (equated monthly installment) for a loan, "
            + "given the principal amount, annual interest rate, and tenure in years.")
    public String calculateEmi(
            @ToolParam(description = "Loan principal amount") double principal,
            @ToolParam(description = "Annual interest rate as a percentage, e.g. 8.5 for 8.5%") double annualRatePercent,
            @ToolParam(description = "Loan tenure in years") int tenureYears
    ) {
        log.info("TOOL CALLED: calculateEmi(principal={}, annualRatePercent={}, tenureYears={})",
                principal, annualRatePercent, tenureYears);

        double monthlyRate = annualRatePercent / 12 / 100;
        int totalMonths = tenureYears * 12;

        if (monthlyRate == 0) {
            double emi = principal / totalMonths;
            return formatEmiResult(emi, principal, totalMonths);
        }

        double emi = principal * monthlyRate * Math.pow(1 + monthlyRate, totalMonths)
                / (Math.pow(1 + monthlyRate, totalMonths) - 1);

        return formatEmiResult(emi, principal, totalMonths);
    }

    private String formatEmiResult(double emi, double principal, int totalMonths) {
        double totalPayment = emi * totalMonths;
        double totalInterest = totalPayment - principal;
        return String.format(
                "Monthly EMI: %.2f | Total payment over tenure: %.2f | Total interest paid: %.2f",
                emi, totalPayment, totalInterest
        );
    }

    @Tool(description = "Calculates the future value of a lump sum investment using compound interest, "
            + "given principal, annual interest rate, time in years, and compounding frequency per year.")
    public String calculateCompoundInterest(
            @ToolParam(description = "Initial principal amount") double principal,
            @ToolParam(description = "Annual interest rate as a percentage, e.g. 7 for 7%") double annualRatePercent,
            @ToolParam(description = "Time period in years") int years,
            @ToolParam(description = "Number of times interest is compounded per year, e.g. 12 for monthly, 1 for annually") int compoundingFrequency
    ) {
        log.info("TOOL CALLED: calculateCompoundInterest(principal={}, annualRatePercent={}, years={}, compoundingFrequency={})",
                principal, annualRatePercent, years, compoundingFrequency);

        double rate = annualRatePercent / 100;
        double futureValue = principal * Math.pow(1 + rate / compoundingFrequency, compoundingFrequency * years);
        double interestEarned = futureValue - principal;

        return String.format(
                "Future value: %.2f | Interest earned: %.2f",
                futureValue, interestEarned
        );
    }

    @Tool(description = "Calculates the future value of a monthly SIP (systematic investment plan), "
            + "given the monthly investment amount, expected annual return rate, and duration in years.")
    public String calculateSip(
            @ToolParam(description = "Amount invested every month") double monthlyInvestment,
            @ToolParam(description = "Expected annual rate of return as a percentage, e.g. 12 for 12%") double annualRatePercent,
            @ToolParam(description = "Investment duration in years") int years
    ) {
        log.info("TOOL CALLED: calculateSip(monthlyInvestment={}, annualRatePercent={}, years={})",
                monthlyInvestment, annualRatePercent, years);

        double monthlyRate = annualRatePercent / 12 / 100;
        int totalMonths = years * 12;

        double futureValue = monthlyInvestment
                * ((Math.pow(1 + monthlyRate, totalMonths) - 1) / monthlyRate)
                * (1 + monthlyRate);

        double totalInvested = monthlyInvestment * totalMonths;
        double estimatedReturns = futureValue - totalInvested;

        return String.format(
                "Future value: %.2f | Total invested: %.2f | Estimated returns: %.2f",
                futureValue, totalInvested, estimatedReturns
        );
    }
}