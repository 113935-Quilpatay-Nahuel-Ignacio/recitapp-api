-- Migration to remove base_price column from venue_sections table
-- This is part of the refactoring to move pricing logic to event-specific ticket prices

-- Drop the base_price column from venue_sections
ALTER TABLE venue_sections DROP COLUMN IF EXISTS base_price; 