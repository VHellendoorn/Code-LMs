from django.test import TestCase

from geotrek.flatpages.tests.factories import FlatPageFactory


class RESTViewsTest(TestCase):
    def setUp(self):
        FlatPageFactory.create_batch(10, published=True)
        FlatPageFactory.create(published=False)

    def test_records_list(self):
        response = self.client.get('/api/en/flatpages.json')
        self.assertEqual(response.status_code, 200)
        records = response.json()
        self.assertEqual(len(records), 10)

    def test_serialized_attributes(self):
        response = self.client.get('/api/en/flatpages.json')
        records = response.json()
        record = records[0]
        self.assertEqual(
            sorted(record.keys()),
            sorted(['content', 'external_url', 'id', 'last_modified',
                    'media', 'portal', 'publication_date', 'published',
                    'published_status', 'slug', 'source', 'target',
                    'title']))
