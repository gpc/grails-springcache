package musicstore

import grails.plugin.springcache.annotations.Cacheable
import grails.plugin.springcache.annotations.CacheFlush

class AlbumController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

	@Cacheable(modelId = "AlbumController")
    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [albumInstanceList: Album.list(params), albumInstanceTotal: Album.count()]
    }

    def create = {
        def albumInstance = new Album()
        albumInstance.properties = params
        return [albumInstance: albumInstance]
    }

	@CacheFlush(modelId = "AlbumController")
    def save = {
		// TODO: custom data binder to acheive this
		if (params."artist.name") {
			def artistInstance = Artist.findByName(params."artist.name")
			if (artistInstance) params.artist = artistInstance
		}
        def albumInstance = new Album(params)
		println albumInstance
        if (albumInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'album.label', default: 'Album'), albumInstance.id])}"
            redirect(action: "show", id: albumInstance.id)
        }
        else {
            render(view: "create", model: [albumInstance: albumInstance])
        }
    }

	@Cacheable(modelId = "AlbumController")
    def show = {
        def albumInstance = Album.get(params.id)
        if (!albumInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'album.label', default: 'Album'), params.id])}"
            redirect(action: "list")
        }
        else {
            [albumInstance: albumInstance]
        }
    }

    def edit = {
        def albumInstance = Album.get(params.id)
        if (!albumInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'album.label', default: 'Album'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [albumInstance: albumInstance]
        }
    }

	@CacheFlush(modelId = "AlbumController")
    def update = {
        def albumInstance = Album.get(params.id)
        if (albumInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (albumInstance.version > version) {
                    
                    albumInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'album.label', default: 'Album')] as Object[], "Another user has updated this Album while you were editing")
                    render(view: "edit", model: [albumInstance: albumInstance])
                    return
                }
            }
            albumInstance.properties = params
            if (!albumInstance.hasErrors() && albumInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'album.label', default: 'Album'), albumInstance.id])}"
                redirect(action: "show", id: albumInstance.id)
            }
            else {
                render(view: "edit", model: [albumInstance: albumInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'album.label', default: 'Album'), params.id])}"
            redirect(action: "list")
        }
    }

	@CacheFlush(modelId = "AlbumController")
    def delete = {
        def albumInstance = Album.get(params.id)
        if (albumInstance) {
            try {
                albumInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'album.label', default: 'Album'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'album.label', default: 'Album'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'album.label', default: 'Album'), params.id])}"
            redirect(action: "list")
        }
    }
}
