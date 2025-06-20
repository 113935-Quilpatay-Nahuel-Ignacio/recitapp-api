-- Fix existing image URLs to include /api context path
-- This script updates URLs that are missing the /api prefix

UPDATE events 
SET flyer_image = REPLACE(flyer_image, 'http://localhost:8080/uploads/', 'http://localhost:8080/api/uploads/')
WHERE flyer_image IS NOT NULL 
  AND flyer_image LIKE 'http://localhost:8080/uploads/%';

UPDATE events 
SET sections_image = REPLACE(sections_image, 'http://localhost:8080/uploads/', 'http://localhost:8080/api/uploads/')
WHERE sections_image IS NOT NULL 
  AND sections_image LIKE 'http://localhost:8080/uploads/%';

-- Show updated URLs for verification
SELECT id, name, flyer_image, sections_image 
FROM events 
WHERE id = 6; 